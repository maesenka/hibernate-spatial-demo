package com.example;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	public void saveAll(List<Trajectory> trajectories) {
		trajectories.forEach( entityManager::persist);
		entityManager.flush();
		entityManager.clear();
	}

}
