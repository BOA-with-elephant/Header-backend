package com.header.header.auth.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ApiResponse {

    // 성공 응답
    SUCCESS_MODIFY_USER("회원 정보가 성공적으로 수정되었습니다.", HttpStatus.OK),
    SUCCESS_REGISTER_USER("회원가입이 완료되었습니다.", HttpStatus.CREATED),

    // 에러 응답
    DUPLICATE_PHONE("이미 존재하는 전화번호입니다.", HttpStatus.CONFLICT),
    DUPLICATE_ID("이미 존재하는 아이디입니다.", HttpStatus.CONFLICT),

    SAME_PASSWORD("(은)는 이전 비밀번호와 동일합니다.", HttpStatus.BAD_REQUEST),
    SAME_PHONE("(은)는 이전 전화번호와 동일합니다.", HttpStatus.BAD_REQUEST),
    SAME_NAME("(은)는 이전 이름과 동일합니다.", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus status;

    ApiResponse(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}
