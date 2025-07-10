package com.header.header.domain.message.entity;

import com.header.header.domain.message.exception.InvalidBatchException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
    @CreationTimestamp // 애플리케이션 레벨에서 Insert 쿼리가 발생할 때 생성 시간을 자동 주입.
    private Timestamp createdAt;

    public void updateBatchResultsCount(Integer successCount,Integer failCount){
        if(totalCount != (successCount + failCount)){
            throw InvalidBatchException.invalidBatchCode("일치하지 않는 결과 카운트");
        }
        this.successCount = successCount;
        this.failCount = failCount;
    }

}
