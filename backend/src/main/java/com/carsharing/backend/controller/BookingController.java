package com.carsharing.backend.controller;

import com.carsharing.backend.model.Booking;
import com.carsharing.backend.repository.BookingRepository;
import com.carsharing.backend.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingService bookingService;

    // GET /api/bookings
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingService.getAllBookings();
        return ResponseEntity.ok(bookings);
    }

    // GET /api/bookings/5
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        Booking booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }

    // POST /api/bookings
    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Map<String, Object> request) {
        Long passengerId = Long.valueOf(request.get("passengerId").toString());
        Long rideId = Long.valueOf(request.get("rideId").toString());
        Integer seatsBooked = Integer.valueOf(request.get("seatsBooked").toString());

        Booking booking = bookingService.createBooking(passengerId, rideId, seatsBooked);
        return ResponseEntity.status(HttpStatus.CREATED).body(booking);
    }

    // GET /api/bookings/passenger/5 - bookings of a passenger
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Booking> cancelBooking(@PathVariable Long id) {
        Booking booking = bookingService.cancelBooking(id);
        return ResponseEntity.ok(booking);
    }

    @PutMapping("/{id}/confirm")
    public ResponseEntity<Booking> confirmBooking(
            @PathVariable Long id,
            @RequestParam Long driverId) {
        Booking booking = bookingService.confirmBooking(id, driverId);
        return ResponseEntity.ok(booking);
    }

    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<Booking>> getBookingsByPassenger(@PathVariable Long passengerId) {
        List<Booking> bookings = bookingService.getBookingsByPassenger(passengerId);
        return ResponseEntity.ok(bookings);
    }

    // GET /api/bookings/ride/5 - bookings for a ride
    @GetMapping("/ride/{rideId}")
    public ResponseEntity<List<Booking>> getBookingsByRide(@PathVariable Long rideId) {
        List<Booking> bookings = bookingService.getBookingsByRide(rideId);
        return ResponseEntity.ok(bookings);
    }

}