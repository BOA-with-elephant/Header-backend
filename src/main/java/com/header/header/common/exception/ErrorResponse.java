package com.header.header.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * API 에러 응답을 위한 표준 DTO
 *
 * - 모든 도메인에서 일관된 에러 응답 형태를 제공
 * - 클라이언트가 에러를 쉽게 파싱할 수 있도록 구조화
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {

    /**
     * HTTP 상태 코드
     */
    private int status;

    /**
     * 에러 타입 (HTTP 상태 텍스트)
     */
    private String error;

    /**
     * 상세 에러 메시지
     */
    private String message;

    /**
     * 에러가 발생한 API 경로
     */
    private String path;

    /**
     * 에러 발생 시점
     */
    private LocalDateTime timestamp;

    /**
     * 기본 생성자 (타임스탬프 자동 설정)
     */
    public ErrorResponse(int status, String error, String message, String path) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 범용 에러 응답 생성 팩토리 메소드
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(status, error, message, path);
    }

    /**
     * 404 Not Found 에러 응답 생성 팩토리 메소드
     */
    public static ErrorResponse notFound(String message, String path) {
        return new ErrorResponse(404, "Not Found", message, path);
    }

    /**
     * 400 Bad Request 에러 응답 생성 팩토리 메소드
     */
    public static ErrorResponse badRequest(String message, String path) {
        return new ErrorResponse(400, "Bad Request", message, path);
    }

    /**
     * 500 Internal Server Error 에러 응답 생성 팩토리 메소드
     */
    public static ErrorResponse internalServerError(String message, String path) {
        return new ErrorResponse(500, "Internal Server Error", message, path);
    }
}