# What we will build

Image you're at car sharing company. On-board GPS systems transmit periodically the car location every second which get 
stored in the database. You're job is to turn these timestamped point data into trajectories, and create a REST service that
will serve them as GeoJSON.

Our database is (of course) CockroachDB. We're using single instance docker configuration for testing purposes. Don't forget to enable the experimental feature
 < see in hibernate db configs >

For this tutorial we'll make use of the [GeoLife GPS Trajectories](https://research.microsoft.com/en-us/downloads/b16d359d-d164-469e-9fd4-daa38f2b2e13/)
database.

The GeoLife dataset consists of +18000 GPS log files for the trajectory. We will first build a data loader that loads
all the points (or a subset) in a single CockroachDB table.

#The dataloader

We use Spring Initialzr with:
- Spring Data with JPA


Ensure we have 5.4.31:
```
	<properties>
    	<java.version>11</java.version>
    	<hibernate.version>5.4.31.Final</hibernate.version>
  	</properties>
```

Add the hibernate-spatial dependency:

```
	<dependency>
      <groupId>org.hibernate</groupId>
      <artifactId>hibernate-spatial</artifactId>
    </dependency>

```

Create the application.properties

```
```

Create the Entity Class `GPSPoint` and the Spring Data Repository for the entity.

We're now ready to start reading in all the trajectory files.

The Dataloader will expect one argument: the path to the directory that contains the PLT files.

The actual parsing 


# Spring Rest Repository 

- Spring Initializr met:
    - Spring Data with JPA
    - Spring Rest Repositories
    - Spring Web

- add a module:
mvn archetype:generate -DgroupId=com.example -DartifactId=dataloader

# A simple front end  
Voor front-end: Leaflet met uGeoJson Layer plugin



