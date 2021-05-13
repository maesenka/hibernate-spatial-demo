package com.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

import static java.lang.String.format;

public class PLTParser {

	Logger LOG = LoggerFactory.getLogger( PLTParser.class );
	final private Path path;
	final private UUID trajectoryId;
	final private GeometryFactory geometryFactory = new GeometryFactory();

	PLTParser(Path path) {
		this.path = path;
		this.trajectoryId = UUID.randomUUID();
	}

	// Returns null in case of errors
	Trajectory parse() {
		LOG.debug( "Parsing " + path + " on thread" + Thread.currentThread().getName() );
		try {
			var tscos = Files.readString(path)
					.lines()
					.skip( 6 ) //skip the first six lines, per documentation
					.map( this::toTimestampedCoordinate )
					.filter( Objects::nonNull )
					.collect( Collectors.toList() );
			return buildTrajectory( tscos );
		}
		catch (IOException e) {
			LOG.warn( format("Failure to read file %s", path), e );
			return null;
		}
	}

	private Trajectory buildTrajectory(List<TSCoordinate> coordinates) {
		if ( coordinates.size() < 2 ) {
			return null;
		}
		LineString ls = buildLineString( coordinates );

		List<LocalDateTime> times = coordinates.stream()
				.map( l -> l.timestamp )
				.collect( Collectors.toList() );
		LocalDateTime start = Collections.min( times );
		LocalDateTime stop = Collections.max( times );
		return new Trajectory( ls, start, stop, this.trajectoryId );
	}

	private LineString buildLineString(List<TSCoordinate> coordinates) {
		List<Coordinate> coords = coordinates.stream().map( l -> l.coordinate ).collect( Collectors.toList() );
		LineString ls = geometryFactory.createLineString( coords.toArray( Coordinate[]::new ) );
		ls.setSRID( 4326 );
		return ls;
	}


	TSCoordinate toTimestampedCoordinate(String pltline) {
		String[] elems = pltline.split( "," );

		try {
			//we store points in lon-lat format,
			//this means the x coordinate is longitude, and the y coordinate latitude
			Coordinate co = new Coordinate(
					Double.parseDouble( elems[1] ),
					Double.parseDouble( elems[0] )
			);
			LocalDateTime timestamp = LocalDateTime.parse( elems[5] + "T" + elems[6] );
			return new TSCoordinate( co, timestamp );
		}
		catch (Throwable t) {
			LOG.warn( "Failure to parse line", t );
			return null;
		}
	}
}

class TSCoordinate {
	final Coordinate coordinate;
	final LocalDateTime timestamp;

	TSCoordinate(Coordinate coordinate, LocalDateTime timestamp) {
		this.coordinate = coordinate;
		this.timestamp = timestamp;
	}
}
