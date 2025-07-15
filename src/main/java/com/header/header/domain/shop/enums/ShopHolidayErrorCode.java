package com.header.header.domain.shop.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShopHolidayErrorCode {

    DATE_UNAVAILABLE("DATE_UNAVAILABLE", "해당 날짜는 예약이 불가능합니다."),
    DATE_HAS_RESV("DATE_HAS_RESV", "해당 날짜에는 예약이 존재합니다. 예약을 취소한 후 다시 시도하세요."),
    INPUT_DATE_WRONG("INPUT_DATE_WRONG", "휴일 시작 날짜는 휴일 종료 날짜보다 앞이어야 합니다."),
    HOL_NOT_FOUND("HOL_NOT_FOUND", "수정할 휴일 정보를 찾을 수 없습니다.");

    private final String code;
    private final String message;
}
