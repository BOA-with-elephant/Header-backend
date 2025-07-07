package com.header.header.domain.message.exception;

public class InvalidTemplateException  extends RuntimeException {
    public InvalidTemplateException(String message) {
        super(message);
    }

    public InvalidTemplateException(String message, Throwable cause) {
        super(message,cause);
    }

    // === 정적 팩토리 메서드들 ===

    // 타입 관련 예외
    public static InvalidTemplateException invalidType(String message) {
        return new InvalidTemplateException("템플릿 타입 오류: " + message);
    }


    // 내용 관련 예외
    public static InvalidTemplateException invalidContent(String message) {
        return new InvalidTemplateException("템플릿 내용 오류: " + message);
    }

    // 플레이스홀더 관련 예외
    public static InvalidTemplateException invalidPlaceholder(String invalidPlaceholder) {
        return new InvalidTemplateException("유효하지 않은 플레이스홀더: " + invalidPlaceholder);
    }

    // 권한 관련 예외
    public static InvalidTemplateException unauthorized(String message) {
        return new InvalidTemplateException("권한 오류: " + message);
    }

    // 필수값 누락 예외
    public static InvalidTemplateException missingRequired(String fieldName) {
        return new InvalidTemplateException("필수값 누락: " + fieldName + "이(가) 필요합니다.");
    }


    public static InvalidTemplateException notFound(String message) {
        return new InvalidTemplateException("유효하지 않은 템플릿 코드" + message);
    }
}
