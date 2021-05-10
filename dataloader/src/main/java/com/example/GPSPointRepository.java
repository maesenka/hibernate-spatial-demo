package com.example;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

public interface GPSPointRepository extends CrudRepository<GPSPoint, UUID> {
}
