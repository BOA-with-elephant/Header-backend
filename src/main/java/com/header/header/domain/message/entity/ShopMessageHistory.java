package com.header.header.domain.message.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name="tbl_shop_msg_history")
@Getter
@NoArgsConstructor( access = AccessLevel.PROTECTED)
public class ShopMessageHistory {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Integer historyCode;
    private Integer batchCode;
    private Integer userCode;

    @Lob
    private String msgContent;
    private String sentStatus;
    private String errorMessage;
    /* 예약 메세지일 경우에는 sentStatus가 reservation이었다가 sent로 바뀌게 된다 따라서 history의 Timestamp는 어노테이션으로
        관리하지 않는다. */
    private Timestamp sentAt;

}
