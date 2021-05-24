
CockroachDB has gained powerful [spatial data capabilities since the 20.2 release](https://www.cockroachlabs.com/blog/how-we-built-spatial-indexing/). One 
of the great things about this is that the spatial features are compatible with the excellent PostGIS spatial data extension for Postgresql (with some [caveats](https://www.cockroachlabs.com/docs/v20.2/spatial-data#compatibility)). Postgis is in my opinion the gold standard for spatial data management.

Hibernate introduced in [version 5.4.30.Final](https://in.relation.to/2021/03/19/hibernate-orm-5430-final-release/) a spatial dialect for CockroachDB. This dialect
supports  the  spatial features of CockroachDB. So your favorite ORM solution can now work as easily with spatial data as with ordinary, non-spatial data. 


To demonstrate what this means we'll build two Spring Boot applications. 
First we'll create a CLI application that loads the GPS trajectory dataset from the [Geolife project](https://research.microsoft.com/en-us/downloads/b16d359d-d164-469e-9fd4-daa38f2b2e13/) in a CockroachDB table. Then we'll create a basic web mapping application that displays the trajectories on a map. The full source code is [available on GitHub](https://research.microsoft.com/en-us/downloads/b16d359d-d164-469e-9fd4-daa38f2b2e13).


#The Dataloader

The Geolife dataset consists of about 18000 GPS log files, each one representing a single trajectory. The Dataloader application
will read each file, extract the GPS coordinates and timestamps, and create a `Trajectory` object from the data which is then persisted in a `trajectory` table in the database.


Our database is (of course) CockroachDB. For this demo we will use a single instance docker configuration. 

```
$ docker run -d --name=cockroach -p 26257:26257 -p 8080:8080 cockroachdb/cockroach:v21.1.0 start-single-node --insecure
```

To create our application, we use the [Spring initializr](https://start.spring.io/) for maven and select the Spring Data JPA dependency. This
will bring in Hibernate as a dependency. The spatial dialects are packaged in the optional hibernate-spatial module. So we need to add this 
dependency to the POM ourselves.

```
    <dependency>
      <groupId>org.hibernate</groupId>
      <artifactId>hibernate-spatial</artifactId>
    </dependency>
```

To finalize the setup of our project, we create the application.properties file 

```
#It's not a web app 
spring.main.web-application-type=NONE
...
spring.jpa.properties.hibernate.dialect=org.hibernate.spatial.dialect.cockroachdb.CockroachDB202SpatialDialect
spring.datasource.url=jdbc:postgresql://localhost:26257/defaultdb?sslmode=disable
spring.datasource.username=root
spring.datasource.password=
```

Note that you need to set the dialect explicitly. If we didn't do this, Spring Boot will choose 
a non-spatial dialect by default for your database.

A `SpatialDialect` extends its base Dialect class by adding support for Geometry types, 
such as Point, LineString or Polygon. This means that Hibernate will handle the persistence of values of these types 
automatically. The spatial dialects also register a set of spatial functions so that you can use them in JPQL (or HQL) queries.

In fact, Hibernate supports not one but two Geometry libraries: the [Java Topology Suite (JTS)](https://github.com/locationtech/jts) and 
[Geolatte-geom](https://github.com/geolatte/geolatte-geom). JTS is the oldest and most popular choice. It also is the 
gold standard for its computational geometry algorithm (CGA) implementations. Geolatte-geom is a more recent alternative with 
a more modern API design, and a focus on working with spatial data capabilities of modern databases. In this tutorial
we'll be using Geolatte-geom. The code on GitHub has a branch that uses JTS for those interested.

 
There is more detail in Chapter 18 of the [Hibernate User Guide](https://docs.jboss.org/hibernate/orm/5.4/userguide/html_single/Hibernate_User_Guide.html#spatial).

Let's continue building the Dataloader application. 

We need an Entity class to represent trajectories. The following will do.
```java
@Entity
public class Trajectory {

  @Id
  @GeneratedValue
  private UUID id;

  private LineString<G2D> geometry;

  private LocalDateTime start;

  private Long durationInMinutes;

  public Trajectory(){}

  public Trajectory(LineString<G2D> lineString, LocalDateTime start, LocalDateTime stop) {
    this.geometry = lineString;
    this.start = start;
    this.durationInMinutes = Duration.between( start, stop ).toMinutes();
  }

}
```
The `Trajectory` class has a `LineString<G2D>` member variable for the trajectory
geometry. This means that a trajectory is spatially represented as a LineString (a connected
series of line segments) in a two-dimensional geographic coordinate reference system. 

Next, we will create a `Repository` for Trajectory. Again, using Hibernate and Spring Boot makes this
almost too easy. The full implementation is shown below.

```java
@Transactional
@Repository
public class TrajectoryRepository {

  @PersistenceContext
  private EntityManager entityManager;

  public TrajectoryRepository() {
  }

  @Transactional
  public void saveAll(List<Trajectory> trajectories) {
    trajectories.forEach( entityManager::persist);
    entityManager.flush();
    entityManager.clear();
  }

}
```

The  Geolife dataset stores trajectories as PLT text files. The `PltParser` class reads the data
and turns it into a `Trajectory` instance.  
```java
public class PltParser {
    ...

  // Returns null in case of errors
  Trajectory parse() {
    try {
      var positions = PositionSequenceBuilders.variableSized( G2D.class );
      List<LocalDateTime> times = new ArrayList<>();
      Files.readString( path )
              .lines()
              .skip( 6 ) //skip the first six lines as per documentation
              .map( this::toTimestampedCoordinate ) //here we parse the GPS log record
              .filter( Objects::nonNull ) //filter out lines we couldn't parse
              .forEach( tsco -> {
                positions.add( tsco.lon, tsco.lat );
                times.add( tsco.timestamp );
              } );
      return buildTrajectory( positions, times );
    }
    catch (IOException e) {
      LOG.warn( format( "Failure to read file %s", path ), e );
      return null;
    }
  }

  private Trajectory buildTrajectory(PositionSequenceBuilder<G2D> seqBuilder, List<LocalDateTime> times) {
    if ( times.size() < 2 ) {
      return null;
    }
    var ls = new LineString<>( seqBuilder.toPositionSequence(), WGS84 );
    var start = Collections.min( times );
    var stop = Collections.max( times );
    return new Trajectory( ls, start, stop );
  }
  
  ...
}
```

The `PltParser` reads the data lines and extracts the longitude, latitude and timestamp in to a helper 
`TimeStampedCoordinate` (not shown). The coordinates of each `TimestampedCoordinate` are then collected
in a `PositionSequenceBuilder`, and the timestamps in a `List`. The `buildTrajectory()` method
builds these data structures into a `Trajectory` object. 

Finally we can complete the Dataloader by walking over the files in the data directory, parse each file and store the
resulting `Trajectory`. To make this a bit more interesting we'll use Reactor to perform the parsing and persisting
in parallel.

The core logic of the Dataloader is the `processPaths` method. It takes as argument a stream of `Path`s
and parses the files in parallel. The results are merged to a single stream, which is then buffered 
in batches of 64 trajectories. These batches are then in parallel persisted to the Cockroach database.

```java
    private Flux<?> processPaths(Flux<Path> paths) {
		return paths.parallel( PARALLELISM )
				.runOn( Schedulers.parallel() )
				.map( PltParser::new )
				.map( PltParser::parse )
				.filter( Objects::nonNull )  
				.sequential()  //merge result
				.buffer( 64 )
				.parallel( PARALLELISM )
				.runOn( Schedulers.parallel() )
				.doOnNext( repository::saveAll )
				.sequential();
	}
```

Let's see it in operation:

```bash
$ mvn package
$ java -jar java -jar target/dataloader-0.0.1-SNAPSHOT.jar $GEOLIFE_DATA_DIRECTORY
.   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.4.5)

2021-05-24 16:32:14.825  INFO 172641 --- [           main] com.example.DataloaderApp                : Starting DataloaderApp v0.0.1-SNAPSHOT using Java 11.0.10 on threadripper1 with PID 172641 (/home/maesenka/workspaces/spins/route-analyser/dataloader/target/dataloader-0.0.1-SNAPSHOT.jar started by maesenka in /home/maesenka/workspaces/spins/route-analyser/dataloader)
2021-05-24 16:32:14.827  INFO 172641 --- [           main] com.example.DataloaderApp                : No active profile set, falling back to default profiles: default
2021-05-24 16:32:15.134  INFO 172641 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Bootstrapping Spring Data JPA repositories in DEFAULT mode.
2021-05-24 16:32:15.141  INFO 172641 --- [           main] .s.d.r.c.RepositoryConfigurationDelegate : Finished Spring Data repository scanning in 2 ms. Found 0 JPA repository interfaces.
2021-05-24 16:32:15.447  INFO 172641 --- [           main] o.hibernate.jpa.internal.util.LogHelper  : HHH000204: Processing PersistenceUnitInfo [name: default]
2021-05-24 16:32:15.500  INFO 172641 --- [           main] org.hibernate.Version                    : HHH000412: Hibernate ORM core version 5.4.31.Final
2021-05-24 16:32:15.571  INFO 172641 --- [           main] o.h.spatial.integration.SpatialService   : HHH80000001: hibernate-spatial integration enabled : true
2021-05-24 16:32:15.591  INFO 172641 --- [           main] o.hibernate.annotations.common.Version   : HCANN000001: Hibernate Commons Annotations {5.1.2.Final}
2021-05-24 16:32:15.678  INFO 172641 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Starting...
2021-05-24 16:32:15.735  INFO 172641 --- [           main] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Start completed.
2021-05-24 16:32:15.770  INFO 172641 --- [           main] org.hibernate.dialect.Dialect            : HHH000400: Using dialect: org.hibernate.spatial.dialect.cockroachdb.CockroachDB202SpatialDialect
2021-05-24 16:32:16.196  INFO 172641 --- [           main] o.h.e.t.j.p.i.JtaPlatformInitiator       : HHH000490: Using JtaPlatform implementation: [org.hibernate.engine.transaction.jta.platform.internal.NoJtaPlatform]
2021-05-24 16:32:16.203  INFO 172641 --- [           main] j.LocalContainerEntityManagerFactoryBean : Initialized JPA EntityManagerFactory for persistence unit 'default'
2021-05-24 16:32:16.287  INFO 172641 --- [           main] com.example.DataloaderApp                : Started DataloaderApp in 1.75 seconds (JVM running for 2.079)
2021-05-24 16:32:16.289  INFO 172641 --- [           main] com.example.DataloaderApp                : Parallelism set at: 16
2021-05-24 16:32:16.289  INFO 172641 --- [           main] com.example.DataloaderApp                : Batchsize set at: 128
Parsing all files took (ms): 6733
2021-05-24 16:32:23.029  INFO 172641 --- [extShutdownHook] j.LocalContainerEntityManagerFactoryBean : Closing JPA EntityManagerFactory for persistence unit 'default'
2021-05-24 16:32:23.033  INFO 172641 --- [extShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown initiated...
2021-05-24 16:32:23.037  INFO 172641 --- [extShutdownHook] com.zaxxer.hikari.HikariDataSource       : HikariPool-1 - Shutdown completed. 
```

Let's inspect the result:

```
$ psql -U root -h localhost -p 26257 -d defaultdb

defaultdb=# select count(*) from trajectory;
 count
-------
 18670
(1 row)

```

We've read 18760 data files, parsed them and stored them in a database in less than 7 seconds making full use of the 
cores we have available. 

# Spring Rest Repository 

- Spring Initializr met:
    - Spring Data with JPA
    - Spring Rest Repositories
    - Spring Web

- add a module:
mvn archetype:generate -DgroupId=com.example -DartifactId=dataloader

# A simple front end  

Quickstart OpenLayers
Using https://github.com/openlayers/ol-webpack to get a minimal build env.



