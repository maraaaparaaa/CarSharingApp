package com.carsharing.backend.repository;

import com.carsharing.backend.model.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {

    // finds rides by route
    // SQL: SELECT * FROM rides WHERE start_location = ? AND end_location = ?
    List<Ride> findByStartLocationAndEndLocation(String startLocation, String endLocation);

    // finds all rides of a certain driver
    // SQL: SELECT * FROM rides WHERE driver_id = ?
    List<Ride> findByDriverId(Long driverId);

    // finds rides by date
    // SQL: SELECT * FROM rides WHERE departure_time > ?
    List<Ride> findByDepartureTimeAfter(LocalDateTime dateTime);

    // finds available rides by no of seats
    // SQL: SELECT * FROM rides WHERE available_seats > 0
    List<Ride> findByAvailableSeatsGreaterThan(Integer seats);

    void deleteAllByDriverId(Long driverId);
}