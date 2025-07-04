package com.header.header.domain.message.enitity;

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
    private int templeteCode;
    private Integer shopCode;
    private String templateContent;
    private String templateType;
}
