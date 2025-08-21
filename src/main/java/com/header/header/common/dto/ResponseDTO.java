package com.header.header.common.dto;

import org.springframework.http.HttpStatus;

public class ResponseDTO {

    private int status;         // HTTP 응답 상태코드
    private String message;     // HTTP 응답 메시지
    private Object data;        // HTTP 응답 데이터

    public ResponseDTO() {
    }

    /* 설명. HttpStatus 클래스 사용
     *  HttpStatus enum 타입에서 String으로 매핑된 int값을 뽑기 위해 value() 메서드 사용.
     * */
    public ResponseDTO(HttpStatus status, String message, Object data) {
        this.status = status.value();
        this.message = message;
        this.data = data;
    }

    public ResponseDTO(HttpStatus status, String message) {
        this.status = status.value();
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResponseDTO{" +
                "status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}