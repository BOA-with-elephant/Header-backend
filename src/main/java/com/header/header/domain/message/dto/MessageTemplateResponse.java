package com.header.header.domain.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageTemplateResponse {
    private String type;

    private List<MessageTemplateSimpleDto> templates;
}
