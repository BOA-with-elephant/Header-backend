package com.header.header.domain.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageReceiver {
    private String sentAt;
    private String name;
    private String sentStatus;
    private String etc; // 비고 : 에러 메세지
}
