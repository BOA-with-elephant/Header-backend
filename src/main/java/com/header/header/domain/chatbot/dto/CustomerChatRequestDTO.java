package com.header.header.domain.chatbot.dto;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CustomerChatRequestDTO {

    @NotBlank(message = "메시지 내용이 비어있습니다.")
    @Size(max = 1000, message = "메시지가 너무 깁니다. (최대 1000자)")
    private String message;
    
    @Size(max = 50, message = "메시지 타입이 너무 깁니다.")
    private String messageType;

}