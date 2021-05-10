package com.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App implements CommandLineRunner {

	private static Logger LOG = LoggerFactory.getLogger( App.class );

	public static void main(String[] args) {
		LOG.info( "STARTING Dataloader...." );
		SpringApplication.run( App.class, args );
		LOG.info( "Dataloader stopped" );
	}

	@Override
	public void run(String... args) throws Exception {
		walk( args[0] ).forEach( System.out::println );
	}

	public void walk(String directory) throws IOException {
		Path root = Path.of( directory );

		Stream<List<GPSPoint>> stream = Files.walk( root )
				.filter( p -> p.toString().endsWith( "plt" ) )
				.map( PLTParser::new )
				.map( PLTParser::parse );
	}

}
