package com.header.header.domain.message.dto;

import com.header.header.domain.message.enums.TemplateType;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageTemplateDTO {
    private Integer templateCode;
    private Integer shopCode;
    private String templateTitle;
    private String templateContent;
    private TemplateType templateType;
}
