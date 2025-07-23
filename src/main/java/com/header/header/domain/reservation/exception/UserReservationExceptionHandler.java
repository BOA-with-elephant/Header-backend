package com.header.header.domain.reservation.exception;

import com.header.header.domain.reservation.enums.UserReservationErrorCode;
import lombok.Getter;

@Getter
public class UserReservationExceptionHandler extends RuntimeException {
    private final UserReservationErrorCode uRErrorCode;

    public UserReservationExceptionHandler(UserReservationErrorCode uRErrorCode) {
        super(uRErrorCode.getMessage());
        this.uRErrorCode = uRErrorCode;
    }

    public UserReservationExceptionHandler(UserReservationErrorCode uRErrorCode, String message) {
        super(message);
        this.uRErrorCode = uRErrorCode;
    }

}
