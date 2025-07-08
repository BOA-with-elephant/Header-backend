package com.header.header.domain.message.enitity;

import com.header.header.domain.message.enums.TemplateType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="tbl_message_template")
@Getter
@NoArgsConstructor( access = AccessLevel.PROTECTED)
public class MessageTemplate {
    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Integer templateCode;
    private Integer shopCode;
    private String templateTitle;
    private String templateContent;

    @Enumerated(EnumType.STRING) // DB에 "INFORMATIONAL", "PROMOTIONAL" 문자열로 저장
    private TemplateType templateType;


    public void modifyMessageTemplateContent(String templateContent) {
        this.templateContent = templateContent;
    }
}
