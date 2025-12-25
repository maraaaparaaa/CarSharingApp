package com.carsharing.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RideResponse {

    private Long id;
    private String startLocation;
    private String endLocation;
    private LocalDateTime departureTime;

    private BigDecimal pricePerSeat;
    private Integer availableSeats;

    // info public despre șofer
    private Long driverId;
    private String driverName;
}
