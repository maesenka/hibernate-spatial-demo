package com.example;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.locationtech.jts.geom.Point;

@Entity
public class GPSPoint {

	@Id
	@GeneratedValue
	private UUID id;

	private Point point;

	private LocalDateTime timestamp;

	private UUID trajectory;

	public GPSPoint(){}

	public GPSPoint(Point point, LocalDateTime timestamp, UUID trajectory) {
		this.point = point;
		this.timestamp = timestamp;
		this.trajectory = trajectory;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public UUID getTrajectory() {
		return trajectory;
	}

	public void setTrajectory(UUID trajectory) {
		this.trajectory = trajectory;
	}

	@Override
	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( o == null || getClass() != o.getClass() ) {
			return false;
		}
		GPSPoint gpsPoint = (GPSPoint) o;
		return Objects.equals( id, gpsPoint.id ) && Objects.equals(
				point,
				gpsPoint.point
		) && Objects.equals( timestamp, gpsPoint.timestamp ) && Objects.equals(
				trajectory,
				gpsPoint.trajectory
		);
	}

	@Override
	public int hashCode() {
		return Objects.hash( id, point, timestamp, trajectory );
	}

	@Override
	public String toString() {
		return "GPSPoint{" +
				"point=" + point +
				", timestamp=" + timestamp +
				", trajectory=" + trajectory +
				'}';
	}
}
