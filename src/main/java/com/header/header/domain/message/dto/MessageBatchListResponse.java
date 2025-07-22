package com.header.header.domain.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageBatchListResponse {
    private Integer id; // batchCode
    private String date;
    private String time;
    private String type;
    private String subject;
}
