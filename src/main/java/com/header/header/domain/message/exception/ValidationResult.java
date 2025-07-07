package com.header.header.domain.message.exception;

import lombok.Getter;

@Getter
public class ValidationResult {
    private final boolean valid;
    private final String errorMessage;

    private ValidationResult(boolean valid, String errorMessage){
        this.valid = valid;
        this.errorMessage = errorMessage;
    }

    // 성공 케이스용 정적 메서드
    public static ValidationResult success(){
        return new ValidationResult(true, null);
    }

    // 실패 케이스용 정적 메서드
    public static ValidationResult failure(String errorMessage){
        return new ValidationResult(false, errorMessage);
    }

    public boolean hasErrorMessage(){
        return errorMessage != null && !errorMessage.trim().isEmpty();
    }

    @Override
    public String toString(){
        return valid ? "ValidationResult{valid=true}"
                : "ValidationResult{valid=false, errorMessage='" + errorMessage + "'}";
    }
}
