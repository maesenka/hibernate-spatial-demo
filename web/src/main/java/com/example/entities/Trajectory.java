package com.example.entities;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.geolatte.geom.Geometry;
import org.geolatte.geom.LineString;


@Entity
public class Trajectory {

	@Id
	@GeneratedValue
	private UUID id;

	private Geometry<?> geometry;

	@JsonFormat(pattern = "yyyy-MM-dd hh:mm:ss")
	private LocalDateTime start;

	private Long durationInMinutes;

	public Trajectory(){}

	public Trajectory(LineString<?> lineString, LocalDateTime start, LocalDateTime stop, UUID trajectory) {
		this.geometry = lineString;
		this.start = start;
		this.durationInMinutes = Duration.between( start, stop ).toMinutes();
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

}