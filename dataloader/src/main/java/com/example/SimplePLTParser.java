package com.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.geolatte.geom.G2D;
import org.geolatte.geom.LineString;
import org.geolatte.geom.PositionSequenceBuilder;
import org.geolatte.geom.PositionSequenceBuilders;
import org.geolatte.geom.crs.CoordinateReferenceSystem;
import org.geolatte.geom.crs.CoordinateReferenceSystems;

import static java.lang.String.format;

public class SimplePLTParser {

	final static CoordinateReferenceSystem<G2D> WGS84 = CoordinateReferenceSystems.WGS84;

	Logger LOG = LoggerFactory.getLogger( SimplePLTParser.class );
	final private Path path;
	final private UUID trajectoryId;

	SimplePLTParser(Path path) {
		this.path = path;
		this.trajectoryId = UUID.randomUUID();
	}

	// Returns null in case of errors
	Trajectory parse() {
		LOG.debug( "Parsing " + path + " on thread" + Thread.currentThread().getName() );
		try {
			var seqBuilder = PositionSequenceBuilders.variableSized( G2D.class );
			List<LocalDateTime> times = new ArrayList<>();
			Files.readString( path )
					.lines()
					.skip( 6 ) //skip the first six lines as per documentation
					.map( this::toTimestampedCoordinate )
					.filter( Objects::nonNull)
					.forEach( tsco -> {
						seqBuilder.add( tsco.lon, tsco.lat );
						times.add( tsco.timestamp );
					});
			return buildTrajectory( seqBuilder, times );
		}
		catch (IOException e) {
			LOG.warn( format( "Failure to read file %s", path ), e );
			return null;
		}
	}

	private Trajectory buildTrajectory(PositionSequenceBuilder<G2D> seqBuilder, List<LocalDateTime> times){
		if ( times.size() < 2 ) {
			return null;
		}
		LineString<G2D> ls = new LineString<>( seqBuilder.toPositionSequence(), WGS84 );
		LocalDateTime start = Collections.min( times );
		LocalDateTime stop = Collections.max( times );
		return new Trajectory( ls, start, stop, this.trajectoryId );
	}

	TSCoordinate toTimestampedCoordinate(String pltline) {
		String[] elems = pltline.split( "," );

		try {
			LocalDateTime timestamp = LocalDateTime.parse( elems[5] + "T" + elems[6] );
			return new TSCoordinate(
					Double.parseDouble( elems[1] ),
					Double.parseDouble( elems[0] ),
					timestamp
			);
		}
		catch (Throwable t) {
			LOG.warn( "Failure to parse line", t );
			return null;
		}
	}
}

class TSCoordinate {
	final double lon;
	final double lat;
	final LocalDateTime timestamp;

	TSCoordinate(double lon, double lat, LocalDateTime timestamp) {
		this.lon = lon;
		this.lat = lat;
		this.timestamp = timestamp;
	}
}
