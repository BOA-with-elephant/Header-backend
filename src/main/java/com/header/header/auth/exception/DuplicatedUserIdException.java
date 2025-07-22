package com.header.header.auth.exception;

public class DuplicatedUserIdException extends RuntimeException {
    public DuplicatedUserIdException(String message) {
        super(message);
    }
}
