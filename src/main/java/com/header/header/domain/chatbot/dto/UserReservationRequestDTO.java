package com.header.header.domain.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserReservationRequestDTO {

    @NotBlank(message = "메시지 내용이 비어있습니다.")
    @Size(max = 1000, message = "메시지가 너무 깁니다. (최대 1000자)")
    private String message;
}
