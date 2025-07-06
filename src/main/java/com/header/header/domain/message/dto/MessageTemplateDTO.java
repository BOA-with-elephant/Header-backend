package com.header.header.domain.message.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MessageTemplateDTO {
    private Integer templeteCode;
    private Integer shopCode;
    private String templateContent;
    private String templateType;
}
