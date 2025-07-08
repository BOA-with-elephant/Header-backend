package com.header.header.domain.message.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

@Setter
@Getter
@ToString
public class MessageSendBatchDTO {

    private Integer batchCode;
    private Integer shopCode;
    private Integer templateCode;
    private Date sendDate;
    private Time sendTime;
    private String sendType;
    private String subject;
    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    private Timestamp createdAt;

}
