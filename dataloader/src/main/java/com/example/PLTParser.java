package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;

public class PLTParser {

	Logger LOG = LoggerFactory.getLogger( PLTParser.class );
	final private Path path;
	final private UUID trajectoryId;
	final private GeometryFactory geometryFactory = new GeometryFactory();

	PLTParser(Path path) {
		this.path = path;
		this.trajectoryId = UUID.randomUUID();
		LOG.info( path.toAbsolutePath().toString() );
	}

	Optional<Trajectory> parse() {
		//for simplicity we first read all GPS points in a list and only if that fully succeeds we
		try (BufferedReader reader = Files.newBufferedReader( path )) {
			//first ensure that we read all lines before we close the reader
			List<TSCoordinate> coordinates = reader.lines()
					.skip( 6 ) //skip the first six lines, per documentation
					.map( this::toTimestampedCoordinate )
					.filter( Objects::nonNull )
					.collect( Collectors.toList() );
			return Optional.ofNullable( buildTrajectory( coordinates ) );
		}
		catch (IOException e) {
			LOG.warn( "Failure to open file " + path.toAbsolutePath(), e );
			return Optional.empty();
		}
	}

	private Trajectory buildTrajectory(List<TSCoordinate> coordinates) {
		if ( coordinates.size() < 2 ) {
			return null;
		}
		List<Coordinate> coords = coordinates.stream().map( l -> l.coordinate ).collect( Collectors.toList() );
		LineString ls = geometryFactory.createLineString( coords.toArray( new Coordinate[] {} ) );
		//using get() is save because we first checked for coordinate size
		LocalDateTime start = coordinates.stream().map( l -> l.timestamp ).min( Comparator.naturalOrder() ).get();
		LocalDateTime stop = coordinates.stream().map( l -> l.timestamp ).max( Comparator.naturalOrder() ).get();
		return new Trajectory( ls, start, stop, this.trajectoryId );
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
