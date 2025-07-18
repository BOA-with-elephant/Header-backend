package com.header.header.domain.shop.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class ErrorResponseMessage {
    // 에러가 발생했을 때 답변할 ResponseMessage 클래스

    private String errorCode;
    private String errorMessage;
}
