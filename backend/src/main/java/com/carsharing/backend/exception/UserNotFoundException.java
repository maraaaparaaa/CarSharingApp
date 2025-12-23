package com.carsharing.backend.exception;

public class UserNotFoundException extends ApiException {
    public UserNotFoundException(Long id) {
        super("user with id " + id + " not found");
    }
}
