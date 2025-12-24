package com.carsharing.backend.service;

import com.carsharing.backend.exception.InvalidBookingException;
import com.carsharing.backend.exception.ResourceNotFoundException;
import com.carsharing.backend.model.Booking;
import com.carsharing.backend.model.Ride;
import com.carsharing.backend.model.User;
import com.carsharing.backend.repository.BookingRepository;
import com.carsharing.backend.repository.RideRepository;
import com.carsharing.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RideRepository rideRepository;
    private final UserRepository userRepository;

    /**
     * Creates new booking with all necessary validation
     */
    @Transactional
    public Booking createBooking(Long passengerId, Long rideId, Integer seatsBooked) {
        // checks if user and ride exist
        User passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + passengerId + " not found"));

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new ResourceNotFoundException("Ride with id " + rideId + " not found"));

        // checks business rules
        if (passenger.getId().equals(ride.getDriver().getId())) {
            throw new InvalidBookingException("You cannot book your own ride");
        }

        if (ride.getStatus() != Ride.RideStatus.ACTIVE) {
            throw new InvalidBookingException("This ride is not available for booking (status: " + ride.getStatus() + ")");
        }

        if (seatsBooked <= 0) {
            throw new InvalidBookingException("Number of seats must be greater than 0");
        }

        if (ride.getAvailableSeats() < seatsBooked) {
            throw new InvalidBookingException(
                    "Not enough seats available. Requested: " + seatsBooked +
                            ", Available: " + ride.getAvailableSeats());
        }

        if (ride.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new InvalidBookingException("Cannot book a ride that has already departed");
        }

        BigDecimal totalPrice = ride.getPricePerSeat().multiply(BigDecimal.valueOf(seatsBooked));

        //creates booking
        Booking booking = new Booking();
        booking.setPassenger(passenger);
        booking.setRide(ride);
        booking.setSeatsBooked(seatsBooked);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(Booking.BookingStatus.PENDING);

        // updates ride
        ride.setAvailableSeats(ride.getAvailableSeats() - seatsBooked);

        if (ride.getAvailableSeats() == 0) {
            ride.setStatus(Ride.RideStatus.FULL);
        }

        rideRepository.save(ride);

        // saves booking
        return bookingRepository.save(booking);
    }

    /**
     * Anulează o rezervare și returnează locurile la cursă
     */
    @Transactional
    public Booking cancelBooking(Long bookingId) {
        // Găsește booking-ul
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking with id " + bookingId + " not found"));

        // Verifică dacă poate fi anulat
        if (booking.getStatus() == Booking.BookingStatus.CANCELLED) {
            throw new InvalidBookingException("Booking is already cancelled");
        }

        if (booking.getStatus() == Booking.BookingStatus.COMPLETED) {
            throw new InvalidBookingException("Cannot cancel a completed booking");
        }

        // Returnează locurile la cursă
        Ride ride = booking.getRide();
        ride.setAvailableSeats(ride.getAvailableSeats() + booking.getSeatsBooked());

        // Dacă cursa era FULL, o setăm înapoi la ACTIVE
        if (ride.getStatus() == Ride.RideStatus.FULL) {
            ride.setStatus(Ride.RideStatus.ACTIVE);
        }

        rideRepository.save(ride);

        // Marchează booking-ul ca anulat
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        return bookingRepository.save(booking);
    }

    /**
     * Confirmă o rezervare (doar driver-ul poate face asta)
     */
    @Transactional
    public Booking confirmBooking(Long bookingId, Long driverId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking with id " + bookingId + " not found"));

        // Verifică dacă cel care confirmă e driver-ul
        if (!booking.getRide().getDriver().getId().equals(driverId)) {
            throw new InvalidBookingException("Only the driver can confirm this booking");
        }

        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new InvalidBookingException("Only pending bookings can be confirmed");
        }

        booking.setStatus(Booking.BookingStatus.CONFIRMED);
        return bookingRepository.save(booking);
    }

    /**
     * Obține toate rezervările unui pasager
     */
    public List<Booking> getBookingsByPassenger(Long passengerId) {
        if (!userRepository.existsById(passengerId)) {
            throw new ResourceNotFoundException("User with id " + passengerId + " not found");
        }
        return bookingRepository.findByPassengerId(passengerId);
    }

    /**
     * Obține toate rezervările pentru o cursă
     */
    public List<Booking> getBookingsByRide(Long rideId) {
        if (!rideRepository.existsById(rideId)) {
            throw new ResourceNotFoundException("Ride with id " + rideId + " not found");
        }
        return bookingRepository.findByRideId(rideId);
    }

    /**
     * Obține un booking după ID
     */
    public Booking getBookingById(Long bookingId) {
        return bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking with id " + bookingId + " not found"));
    }

    /**
     * Obține toate booking-urile
     */
    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }
}