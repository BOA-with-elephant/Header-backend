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
    RESV_ALREADY_PAID("RESV_ALREADY_PAID", "결제 완료된 예약은 취소할 수 없습니다."),
    USER_NOT_FOUND("USER_NOT_FOUND", "유효하지 않은 사용자 정보입니다."),
    USER_HAS_LEFT("USER_HAS_LEFT", "탈퇴한 사용자 정보입니다"),
    INPUT_DATE_WRONG("INPUT_DATE_WRONG", "조회 시작 날짜는 조회 종료 날짜보다 이전이어야 합니다."),
    SHOP_NOT_FOUND("SHOP_NOT_FOUND", "샵 정보를 찾을 수 없습니다."),
    MENU_NOT_FOUND("MENU_NOT_FOUND", "메뉴 정보를 찾을 수 없습니다.");

    private final String code;
    private final String message;
}
