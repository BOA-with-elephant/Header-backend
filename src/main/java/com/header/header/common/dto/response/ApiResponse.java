package com.header.header.common.dto.response;

public class ApiResponse<T> {
    private String status; // "success" or "error"
    private T data;
    private String message;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.status = "success";
        response.data = data;
        return response;
    }

    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.status = "error";
        response.message = message;
        return response;
    }

    // getter/setter 생략 또는 Lombok 사용 가능
}
