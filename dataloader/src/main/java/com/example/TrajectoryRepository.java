package com.example;

import java.lang.reflect.Method;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public class TrajectoryRepository {

	@PersistenceContext
	private EntityManager entityManager;

	public TrajectoryRepository() {
	}

	@Transactional
	public void save(Trajectory trajectory) {
		entityManager.persist( trajectory );
	}

}
