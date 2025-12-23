package com.carsharing.backend.repository;

import com.carsharing.backend.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // finds all reservations of a passenger
    // SQL: SELECT * FROM bookings WHERE passenger_id = ?
    List<Booking> findByPassengerId(Long passengerId);

    // finds all reservations for a ride
    // SQL: SELECT * FROM bookings WHERE ride_id = ?
    List<Booking> findByRideId(Long rideId);

    // finds all reservations by status
    // SQL: SELECT * FROM bookings WHERE status = ?
    List<Booking> findByStatus(Booking.BookingStatus status);
}