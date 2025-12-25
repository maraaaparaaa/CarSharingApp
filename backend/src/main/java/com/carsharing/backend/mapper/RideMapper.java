package com.carsharing.backend.mapper;

import com.carsharing.backend.dto.RideResponse;
import com.carsharing.backend.model.Ride;
import org.springframework.stereotype.Component;

@Component
public class RideMapper {

    public RideResponse toDto(Ride ride) {
        return new RideResponse(
                ride.getId(),
                ride.getStartLocation(),
                ride.getEndLocation(),
                ride.getDepartureTime(),
                ride.getPricePerSeat(),
                ride.getAvailableSeats(),
                ride.getDriver().getId(),
                ride.getDriver().getFullName()
        );
    }
}
