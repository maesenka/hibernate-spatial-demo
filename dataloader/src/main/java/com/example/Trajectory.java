package com.example;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.locationtech.jts.geom.LineString;

@Entity
public class Trajectory {

	@Id
	@GeneratedValue
	private UUID id;

	private LineString geometry;

	private LocalDateTime start;

	private Duration duration;

	private UUID trajectory;

	public Trajectory(){}

	public Trajectory(LineString lineString, LocalDateTime start, LocalDateTime stop, UUID trajectory) {
		this.geometry = lineString;
		this.start = start;
		this.duration = Duration.between( stop, start );
		this.trajectory = trajectory;
	}

}
