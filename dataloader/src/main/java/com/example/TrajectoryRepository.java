package com.example;

import java.lang.reflect.Method;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static java.lang.String.format;

@Transactional
@Repository
public class TrajectoryRepository {

	private final static Logger LOG = LoggerFactory.getLogger( TrajectoryRepository.class );

	private int counter = 0;

	@PersistenceContext
	private EntityManager entityManager;

	public TrajectoryRepository() {
	}

	public void save(Trajectory trajectory) {
		entityManager.persist( trajectory );
	}

	@Transactional
	public void saveAll(List<Trajectory> trajectories) {
		LOG.debug( format( "Tx %d on thread %s", counter++, Thread.currentThread().getName() ) );
		trajectories.forEach( this::save );
		entityManager.flush();
		entityManager.clear();
	}

}
