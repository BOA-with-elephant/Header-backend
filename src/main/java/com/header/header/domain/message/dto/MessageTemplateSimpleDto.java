package com.header.header.domain.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageTemplateSimpleDto {
    private Integer templateCode;
    private String title;
    private String content;
}
