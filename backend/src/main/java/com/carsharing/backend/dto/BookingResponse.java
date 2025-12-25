package com.carsharing.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class BookingResponse {

    private Long id;

    // passenger info
    private Long passengerId;
    private String passengerName;

    // ride info
    private Long rideId;
    private String startLocation;
    private String endLocation;

    private Integer seatsBooked;
    private BigDecimal totalPrice;
    private String status;
}
