package com.header.header.domain.message.dto;

import com.header.header.domain.message.enums.TemplateType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MessageTemplateDTO {
    private Integer templateCode;
    private Integer shopCode;
    private String templateTitle;
    private String templateContent;
    private TemplateType templateType;
}
