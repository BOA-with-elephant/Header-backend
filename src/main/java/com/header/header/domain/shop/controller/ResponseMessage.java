package com.header.header.domain.shop.controller;

import java.util.Map;

public class ResponseMessage {

    private int httpStatus;
    private String message;
    //데이터 담을 맵
    private Map<String, Object> results;

    public ResponseMessage() {}

    public ResponseMessage(int httpStatus, String message, Map<String, Object> results) {
        this.httpStatus = httpStatus;
        this.message = message;
        this.results = results;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(int httpStatus) {
        this.httpStatus = httpStatus;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, Object> getResults() {
        return results;
    }

    public void setResults(Map<String, Object> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "ResponseMessage{" +
                "httpStatus=" + httpStatus +
                ", message='" + message + '\'' +
                ", results=" + results +
                '}';
    }
}
