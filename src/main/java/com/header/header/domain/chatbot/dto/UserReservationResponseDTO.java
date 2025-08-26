package com.header.header.domain.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
public class UserReservationResponseDTO {

    private String intent;
    private MessagePayload message; // String -> MessagePayload 객체로 변경
    private List<Map<String, Object>> actions;
    private Map<String, Object> data;

    // 중첩된 message 객체를 위한 내부 클래스
    @Getter
    public static class MessagePayload {
        private String text;
    }

    // 최종 답변을 쉽게 가져오기 위한 편의 메소드
    public String getAnswer() {
        if (message != null && message.getText() != null) {
            return message.getText();
        }
        return "";
    }
}
