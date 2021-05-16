package com.example.repositories;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.example.entities.Trajectory;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Polygon;

import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.polygon;
import static org.geolatte.geom.builder.DSL.ring;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;

public class TrajectoryCustomRepositoryImpl implements TrajectoryCustomRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<Trajectory> bbox(String bbox, int zoom) {
		TypedQuery<Trajectory> query = entityManager.createQuery(
				"select t from Trajectory  t where filter(t.geometry, :param) = true ",
				Trajectory.class
		);
		Polygon<G2D> filter = fromBbox( bbox );
		query.setParameter( "param", filter );
		return query.getResultList();
	}

	private Polygon<G2D> fromBbox(String bbox) {
		String[] elems = bbox.split( "," );
		if ( elems.length != 4 ) {
			throw new IllegalArgumentException( "Invalid BBox: expecting exactly 4 coordinates" );
		}
		try {
			List<Double> co = Arrays.stream( elems ).map( Double::parseDouble ).collect( Collectors.toList() );
			double minLon = co.get( 0 );
			double minLat = co.get( 1 );
			double maxLon = co.get( 2 );
			double maxLat = co.get( 3 );
			return polygon(
					WGS84,
					ring(
							g( minLon, minLat ),
							g( minLon, maxLat ),
							g( maxLon, maxLat ),
							g( maxLon, minLat ),
							g( minLon, minLat )
					)
			);
		}
		catch (NumberFormatException e) {
			throw new IllegalArgumentException( "Invalid bbox", e );
		}

	}

}
