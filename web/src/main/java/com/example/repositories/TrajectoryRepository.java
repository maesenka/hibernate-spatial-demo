package com.example.repositories;

import java.util.UUID;

import com.example.entities.Trajectory;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface TrajectoryRepository extends PagingAndSortingRepository<Trajectory, UUID> {

}
