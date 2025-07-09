package com.header.header.domain.reservation.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum UserReservationState {

    APPROVE("예약확정"),
    CANCEL("예약취소"),
    FINISH("시술완료"),
    PAY_COMPLETED("결제완료");

    private final String dbName;
}
