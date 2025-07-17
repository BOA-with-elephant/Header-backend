package com.header.header.common.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final String message;
    private final LocalDateTime timestamp;

    @Builder
    private ApiResponse(boolean success, T data, String message, LocalDateTime timestamp) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, "요청이 성공적으로 처리되었습니다.", LocalDateTime.now());
    }

    public static <T> ApiResponse<T> fail(String message, T errorBody) {
        return new ApiResponse<>(false, errorBody, message, LocalDateTime.now());
    }

    // getter/setter 생략 또는 Lombok 사용 가능
}
