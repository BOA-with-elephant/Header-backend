package com.header.header.auth.exception;

import com.header.header.auth.common.ApiResponse;

public class RegistrationUnknownException extends RuntimeException {
    public RegistrationUnknownException(Exception cause) { // 원인(cause)만 받는 생성자
        super(ApiResponse.UNKNOWN_ERROR.getMessage(), cause);
    }
}
