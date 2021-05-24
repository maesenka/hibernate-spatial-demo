package com.example.repositories;

import java.util.List;

import com.example.entities.Trajectory;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.stereotype.Component;


@Component
public interface TrajectoryCustomRepository {

	/**
	 * Find all {@code Trajectory}s that overlap with the specified bounding box
	 * @param bbox the bounding box as a String (minlon, minlat, maxlon, maxlat)
	 * @return the {@code Trajectory}s that overlap with the specified bounding box
	 */
	@RestResource(path="bbox")
	List<Trajectory> bbox(String bbox);

}
