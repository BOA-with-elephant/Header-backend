package com.header.header.domain.message.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MessageSendBatchDTO {

    private Integer batchCode;
    @NotBlank
    private Integer shopCode;
    private Integer templateCode;
    private Date sendDate;
    private Time sendTime;
    @NotBlank
    private String sendType;
    @NotBlank
    private String subject;
    @NotBlank
    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    private Timestamp createdAt;

}
