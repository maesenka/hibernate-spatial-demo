package com.example.repositories;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.spatial.criterion.SpatialProjections;
import org.hibernate.spatial.criterion.SpatialRestrictions;
import org.hibernate.spatial.predicate.GeolatteFilterPredicate;
import org.hibernate.spatial.predicate.GeolatteSpatialPredicates;
import org.hibernate.spatial.predicate.JTSSpatialPredicates;

import com.example.entities.Trajectory;
import org.geolatte.geom.Envelope;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.Polygon;

import static org.geolatte.geom.builder.DSL.g;
import static org.geolatte.geom.builder.DSL.polygon;
import static org.geolatte.geom.builder.DSL.ring;
import static org.geolatte.geom.crs.CoordinateReferenceSystems.WGS84;

public class TrajectoryCustomRepositoryImpl implements TrajectoryCustomRepository {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public List<Trajectory> intersects(
			double minlon, double minlat, double maxlon, double maxlat) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Trajectory> query = cb.createQuery( Trajectory.class );
		Root<Trajectory> root = query.from( Trajectory.class );
		Polygon<G2D> filterPoly = polygon(
				WGS84,
				ring(
						g( minlon, minlat ),
						g( minlon, maxlat ),
						g( maxlon, maxlat ),
						g( maxlon, minlat ),
						g( minlon, minlat )
				)
		);
		ParameterExpression<Polygon> filterParam = cb.parameter( Polygon.class );
		Predicate pred = GeolatteSpatialPredicates.intersects(
				cb,
				root.get( "geometry" ),
				filterParam
		);

		query.where( pred );

		return entityManager
				.createQuery( query )
				.setParameter( filterParam, filterPoly )
				.getResultList();
	}

}
