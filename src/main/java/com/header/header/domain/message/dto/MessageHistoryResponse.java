package com.header.header.domain.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageHistoryResponse {
    private Integer successCount;
    private Integer failCount;
    private List<MessageReceiver> receivers;
}
