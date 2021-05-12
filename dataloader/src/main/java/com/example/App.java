package com.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class App implements CommandLineRunner {

	final static int NUM_TRAJECTORIES = 100;

	public static void main(String[] args) {
		SpringApplication.run( App.class, args );
	}

	@Autowired
	private TrajectoryRepository repository;

	@Override
	public void run(String... args) throws Exception {
		if ( args[0] == null ) {
			System.err.println( "Argument missing." );
			return;
		}
		walk( args[0] ).forEach( repository::save );
	}

	public Stream<Trajectory> walk(String directory) throws IOException {
		return Files.walk( Path.of( directory ) )
				.filter( p -> p.toString().endsWith( "plt" ) )
				.map( PLTParser::new )
				.map( PLTParser::parse )
				.filter( Optional::isPresent )
				.map( Optional::get );
	}

}
