package com.header.header.domain.sales.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 결제 상태를 나타내는 열거형
 * - 결제의 생명주기에 따른 상태 관리
 */
@Getter
@RequiredArgsConstructor
public enum PaymentStatus {
    COMPLETED,  // 완료
    CANCELLED,  // 전체 취소
    PARTIAL_CANCELLED,  // 부분 취소
    DELETED;    // 삭제(논리삭제로 상태변경)

    public boolean isCancellable() {
        return this == COMPLETED || this == PARTIAL_CANCELLED;
    }

    public boolean isCancelled() {
        return this == CANCELLED || this == PARTIAL_CANCELLED;
    }
}