package com.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

public class PLTParser {

	Logger LOG = LoggerFactory.getLogger( PLTParser.class );
	final private Path path;
	final private UUID trajectory;
	final private GeometryFactory geometryFactory = new GeometryFactory();

	PLTParser(Path path) {
		this.path = path;
		this.trajectory = UUID.randomUUID();
		LOG.info( path.toAbsolutePath().toString() );
	}

	List<GPSPoint> parse() {
		//for simplicity we first read all GPS points in a list and only if that fully succeeds we
		try (BufferedReader reader = Files.newBufferedReader( path )) {
			//first ensure that we read all lines before we close the reader
			return reader.lines()
					.skip( 6 ) //skip the first six lines, per documentation
					.map( this::toGPSPoint )
					.filter( Objects::nonNull )
					.collect( Collectors.toList());
		}
		catch (IOException  e) {
			LOG.warn( "Failure to open file " + path.toAbsolutePath(), e );
			return new ArrayList<>();
		}
	}

	GPSPoint toGPSPoint(String pltline)  {
		String[] elems = pltline.split( "," );

		try {
			//we store points in lon-lat format,
			//this means the x coordinate is longitude, and the y coordinate latitude
			Point pnt = geometryFactory.createPoint( new Coordinate(
					Double.parseDouble( elems[1] ),
					Double.parseDouble( elems[0] )
			) );
			pnt.setSRID( 4326 );
			LocalDateTime timestamp = LocalDateTime.parse( elems[5] + "T" + elems[6] );
			return new GPSPoint( pnt, timestamp, trajectory );
		}
		catch (Throwable t) {
			LOG.warn( "Failure to parse line", t );
			return null;
		}
	}
}
