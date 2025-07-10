package com.header.header.domain.message.enums;

public enum MessageStatus {
    PENDING,
    RESERVED,
    SUCCESS,
    FAIL;

    // 전환 가능한 상태 검증
    public boolean canTransitionTo(MessageStatus newStatus) {
        return switch (this) {
            case PENDING, RESERVED -> newStatus == SUCCESS || newStatus == FAIL;
            case SUCCESS, FAIL -> false; // 최종 상태
            default -> false;
        };
    }
}
