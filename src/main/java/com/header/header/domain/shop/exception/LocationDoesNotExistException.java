package com.header.header.domain.shop.exception;

public class LocationDoesNotExistException extends RuntimeException {
    public LocationDoesNotExistException(String message) {
        super(message);
    }
}
