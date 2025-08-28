package com.header.header.auth.exception;

public class SamePwdException extends RuntimeException {
    public SamePwdException(String message) {
        super(message);
    }
}
