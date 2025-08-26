package com.header.header.domain.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserReservationResponseDTO {

    @NotBlank(message = "응답이 비어 있습니다.")
    private String answer;
}
