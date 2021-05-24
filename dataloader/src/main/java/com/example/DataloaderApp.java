package com.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
public class DataloaderApp implements CommandLineRunner {

	final static Logger LOG = LoggerFactory.getLogger( DataloaderApp.class );

	//you can limit the amount of files (trajectories) to read
	final static int NUM_TRAJECTORIES = Integer.MAX_VALUE;

	//We use 1/4 available threads for parsing the files, and 1/4 for persisting trajectories
	//so this will use up 50% of available CPU resources.
	final static int PARALLELISM = Runtime.getRuntime().availableProcessors() / 4;

	//we create a transaction for each batch
	@Value("128")
	private int batchSize;

	public static void main(String[] args) {
		SpringApplication.run( DataloaderApp.class, args );
	}

	@Autowired
	private TrajectoryRepository repository;

	@Override
	public void run(String... args) {
		if ( args[0] == null ) {
			System.err.println( "Argument missing." );
			return;
		}
		LOG.info( "Parallelism set at: " + PARALLELISM );
		LOG.info( "Batchsize set at: " + batchSize );
		final long start = System.currentTimeMillis();
		walk( args[0] )
				.transform( this::processPaths )
				.doOnError( Throwable::printStackTrace )
				.doOnTerminate( () -> System.out.println( "Parsing all files took (ms): " + ( System.currentTimeMillis() - start ) ) )
				.blockLast();
	}

	private Flux<?> processPaths(Flux<Path> paths) {
		return paths.parallel( PARALLELISM )
				.runOn( Schedulers.parallel() )
				.map( PltParser::new )
				.map( PltParser::parse )
				.filter( Objects::nonNull )
				.sequential()
				.buffer( batchSize )
				.parallel( PARALLELISM )
				.runOn( Schedulers.parallel() )
				.doOnNext( repository::saveAll )
				.sequential();
	}

	private Flux<Path> walk(String directory) {
		return Flux.using(
				() -> mkPathStream( directory ),
				Flux::fromStream,
				Stream::close
		).take( NUM_TRAJECTORIES );
	}

	private Stream<Path> mkPathStream(String directory) {
		try {
			return Files.walk( Path.of( directory ) ).filter( p -> p.toString().endsWith( "plt" ) );
		}
		catch (IOException e) {
			throw new RuntimeException( e );
		}
	}

}
