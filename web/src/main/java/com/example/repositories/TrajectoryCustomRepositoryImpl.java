package com.example.repositories;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
	public List<Trajectory> bbox(String bbox, int zoom) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Trajectory> query = cb.createQuery( Trajectory.class );
		Root<Trajectory> root = query.from( Trajectory.class );
		Envelope<G2D> envelope = fromBbox( bbox );
		GeolatteFilterPredicate filterPred = new GeolatteFilterPredicate( cb, root.get( "geometry" ), envelope );
		query.where( filterPred );

		return entityManager
				.createQuery( query )
				.getResultList();
	}

	private Envelope<G2D> fromBbox(String bbox) {
		String[] elems = bbox.split( "," );
		if (elems.length != 4) {
			throw new IllegalArgumentException("Bbox requires exactly 4 doubles");
		}
		try {
			List<Double> co = Arrays.stream( elems ).map( Double::parseDouble ).collect( Collectors.toList() );
			return new Envelope<>( co.get(0), co.get(1), co.get(2), co.get(3), WGS84 );
		}catch(NumberFormatException e){
			throw new IllegalArgumentException("Bbox requires exactly 4 doubles");
		}

	}

}
