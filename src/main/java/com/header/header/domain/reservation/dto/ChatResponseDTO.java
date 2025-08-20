package com.header.header.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDTO {

    @JsonProperty("session_id")
    private String sessionId;
    private String answer;
}
