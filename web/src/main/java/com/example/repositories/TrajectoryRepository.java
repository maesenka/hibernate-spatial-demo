package com.example.repositories;

import java.util.UUID;

import com.example.entities.Trajectory;
import org.springframework.data.repository.CrudRepository;

public interface TrajectoryRepository extends CrudRepository<Trajectory, UUID>, TrajectoryCustomRepository {

}
