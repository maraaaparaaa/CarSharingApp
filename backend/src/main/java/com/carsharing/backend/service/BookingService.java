package com.carsharing.backend.service;

import com.carsharing.backend.exception.BookingException;
import com.carsharing.backend.exception.RideNotFoundException;
import com.carsharing.backend.exception.UserNotFoundException;
import com.carsharing.backend.model.Booking;
import com.carsharing.backend.model.Ride;
import com.carsharing.backend.model.User;
import com.carsharing.backend.repository.BookingRepository;
import com.carsharing.backend.repository.RideRepository;
import com.carsharing.backend.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Transactional // saves the changes in DB, if something fails, all fail
@Service  // spring auto creates the bean
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final RideRepository rideRepository;

    public Booking createBooking(Long passengerId, Long rideId, int seatsBooked) throws Exception {
        User passenger = userRepository.findById(passengerId)
                .orElseThrow(() -> new UserNotFoundException(passengerId));

        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException(rideId));

        if (passengerId.equals(ride.getDriver().getId()))
            throw new BookingException("Cannot book your own ride");

        if (ride.getStatus() != Ride.RideStatus.ACTIVE)
            throw new BookingException("Ride is not active");

        if (seatsBooked <= 0)
            throw new BookingException("Invalid number of seats");

        if (ride.getAvailableSeats() < seatsBooked)
            throw new BookingException("Not enough available seats");

        BigDecimal totalPrice = ride.getPricePerSeat().multiply(BigDecimal.valueOf(seatsBooked));

        //create new booking
        Booking booking = new Booking();
        booking.setPassenger(passenger);
        booking.setRide(ride);
        booking.setSeatsBooked(seatsBooked);
        booking.setTotalPrice(totalPrice);
        booking.setStatus(Booking.BookingStatus.PENDING);

        //update ride
        int availableSeats = ride.getAvailableSeats();
        availableSeats -= seatsBooked;
        if(availableSeats == 0)
            ride.setStatus(Ride.RideStatus.FULL);
        ride.setAvailableSeats(availableSeats);

        //save
        rideRepository.save(ride);

        return bookingRepository.save(booking);
    }

    public void cancelBooking(Booking booking){
        booking.setStatus(Booking.BookingStatus.CANCELLED);

        Ride ride = booking.getRide();

        ride.setAvailableSeats(ride.getAvailableSeats() + booking.getSeatsBooked());

        if(ride.getStatus() == Ride.RideStatus.FULL)
            ride.setStatus(Ride.RideStatus.ACTIVE);

        bookingRepository.save(booking);
        rideRepository.save(ride);
    }

}
