package com.carsharing.backend.controller;

import com.carsharing.backend.model.Booking;
import com.carsharing.backend.repository.BookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BookingController {

    private final BookingRepository bookingRepository;

    // GET /api/bookings
    @GetMapping
    public ResponseEntity<List<Booking>> getAllBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        return ResponseEntity.ok(bookings);
    }

    // GET /api/bookings/5
    @GetMapping("/{id}")
    public ResponseEntity<Booking> getBookingById(@PathVariable Long id) {
        return bookingRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/bookings
    @PostMapping
    public ResponseEntity<Booking> createBooking(@RequestBody Booking booking) {
        // Setează status implicit
        if (booking.getStatus() == null) {
            booking.setStatus(Booking.BookingStatus.PENDING);
        }

        Booking savedBooking = bookingRepository.save(booking);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedBooking);
    }

    // GET /api/bookings/passenger/5 - bookings of a passenger
    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<Booking>> getBookingsByPassenger(@PathVariable Long passengerId) {
        List<Booking> bookings = bookingRepository.findByPassengerId(passengerId);
        return ResponseEntity.ok(bookings);
    }

    // GET /api/bookings/ride/5 - bookings for a ride
    @GetMapping("/ride/{rideId}")
    public ResponseEntity<List<Booking>> getBookingsByRide(@PathVariable Long rideId) {
        List<Booking> bookings = bookingRepository.findByRideId(rideId);
        return ResponseEntity.ok(bookings);
    }

    // PUT /api/bookings/5/status - update status
    @PutMapping("/{id}/status")
    public ResponseEntity<Booking> updateBookingStatus(
            @PathVariable Long id,
            @RequestParam Booking.BookingStatus status) {

        return bookingRepository.findById(id)
                .map(booking -> {
                    booking.setStatus(status);
                    Booking updatedBooking = bookingRepository.save(booking);
                    return ResponseEntity.ok(updatedBooking);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /api/bookings/5
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        if (!bookingRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        bookingRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}