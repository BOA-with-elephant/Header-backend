package com.header.header.domain.message.exception;

public class InvalidBatchException extends RuntimeException {
    public InvalidBatchException(String message) {
        super(message);
    }

    public InvalidBatchException(String message, Throwable cause) {
        super(message,cause);
    }

    // === 정적 팩토리 메서드들 ===

    // 배치 코드 관련 에러
    public static InvalidBatchException invalidBatchCode(String message) {
        return new InvalidBatchException("배치 코드 오류: " + message);
    }
}
