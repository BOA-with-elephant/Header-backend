package com.header.header.domain.message.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Date;

@Entity
@Table(name = "tbl_msg_send_batch")
@Getter
@NoArgsConstructor( access = AccessLevel.PROTECTED )
public class MessageSendBatch {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
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
