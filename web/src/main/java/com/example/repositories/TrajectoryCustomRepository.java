package com.example.repositories;

import java.util.List;
import java.util.UUID;

import com.example.entities.Trajectory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Component;


@Component
public interface TrajectoryCustomRepository {

	@RestResource(path="intersects")
	List<Trajectory> intersects(double minlon, double minlat, double maxlon, double maxlat);

}
