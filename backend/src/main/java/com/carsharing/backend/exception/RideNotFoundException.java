package com.carsharing.backend.exception;

public class RideNotFoundException extends ApiException {
    public RideNotFoundException(Long id) {
        super("Ride with id " + id + " not found");
    }
}
