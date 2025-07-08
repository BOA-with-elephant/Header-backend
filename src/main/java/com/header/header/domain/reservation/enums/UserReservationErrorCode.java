package com.header.header.domain.reservation.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserReservationErrorCode {

    UNKNOWN_DB_VALUE("UNKNOWN_DB_VALUE", "유효하지 않은 값입니다."),
    RESV_NOT_FOUND("RESV_NOT_FOUND", "예약 정보를 찾을 수 없습니다."),
    RESV_ALREADY_DEACTIVATED("RESV_ALREADY_DEACTIVATED", "이미 취소된 예약입니다."),
    RESV_ALREADY_FINISHED("RESV_ALREADY_FINISHED", "이미 완료된 예약은 취소할 수 없습니다."),
    RESV_ALREADY_PAID("RESV_ALREADY_PAID", "결제 완료된 예약은 취소할 수 없습니다.");

    private final String code;
    private final String message;
}
