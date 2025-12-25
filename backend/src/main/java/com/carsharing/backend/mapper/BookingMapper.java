package com.carsharing.backend.mapper;

import com.carsharing.backend.dto.BookingResponse;
import com.carsharing.backend.model.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingResponse toDto(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getPassenger().getId(),
                booking.getPassenger().getFullName(),
                booking.getRide().getId(),
                booking.getRide().getStartLocation(),
                booking.getRide().getEndLocation(),
                booking.getSeatsBooked(),
                booking.getTotalPrice(),
                booking.getStatus().name()
        );
    }
}
