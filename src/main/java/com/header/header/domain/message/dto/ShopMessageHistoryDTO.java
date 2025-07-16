package com.header.header.domain.message.dto;

import lombok.*;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ShopMessageHistoryDTO {

    private Integer historyCode;
    private Integer batchCode;
    private Integer userCode;

    private String msgContent;
    private String sendStatus;
    private String errorMessage;
    private Timestamp sentAt;
}
