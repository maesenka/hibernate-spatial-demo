package com.example.entities;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.geolatte.geom.Geometry;
import org.geolatte.geom.LineString;


@Entity
public class Trajectory {

	@Id
	@GeneratedValue
	private UUID id;

	private Geometry<?> geometry;

	private LocalDateTime start;

	private Long durationInMinutes;

	private UUID trajectory;

	public Trajectory(){}

	public Trajectory(LineString<?> lineString, LocalDateTime start, LocalDateTime stop, UUID trajectory) {
		this.geometry = lineString;
		this.start = start;
		this.durationInMinutes = Duration.between( start, stop ).toMinutes();
		this.trajectory = trajectory;
	}

	public UUID getId() {
		return id;
	}

	public Geometry<?> getGeometry() {
		return geometry;
	}

	public LocalDateTime getStart() {
		return start;
	}

	public Long getDurationInMinutes() {
		return durationInMinutes;
	}

	public UUID getTrajectory() {
		return trajectory;
	}
}