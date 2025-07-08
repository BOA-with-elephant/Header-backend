package com.header.header.domain.message.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private Timestamp sentAt;

}
