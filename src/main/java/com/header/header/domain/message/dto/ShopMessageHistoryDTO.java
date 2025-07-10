package com.header.header.domain.message.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Setter
@Getter
@ToString
public class ShopMessageHistoryDTO {

    private Integer historyCode;
    private Integer batchCode;
    private Integer userCode;

    private String msgContent;
    private String sentStatus;
    private String errorMessage;
    private Timestamp sentAt;
}
