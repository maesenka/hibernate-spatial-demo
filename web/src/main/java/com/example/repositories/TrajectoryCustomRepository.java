package com.example.repositories;

import java.util.List;

import com.example.entities.Trajectory;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Component;


@Component
public interface TrajectoryCustomRepository {

	@RestResource(path="bbox")
	List<Trajectory> bbox(String bbox);

}
