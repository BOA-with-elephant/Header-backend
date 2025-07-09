package com.header.header.domain.message.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

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

    /* comment. Batch는 사용자가 요청한 시점에 기록한다. */
    private Date sendDate;
    private Time sendTime;

    private String sendType;
    private String subject;
    private Integer totalCount;
    private Integer successCount;
    private Integer failCount;
    @CreationTimestamp // 애플리케이션 레벨에서 Insert 쿼리가 발생할 때 생성 시간을 자동 주입.
    private Timestamp createdAt;

}
