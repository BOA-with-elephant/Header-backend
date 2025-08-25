package com.header.header.domain.chatbot.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CustomerChatRequestDTO {

    private String message;
    private String messageType;

}