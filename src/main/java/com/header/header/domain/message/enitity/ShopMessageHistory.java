package com.header.header.domain.message.enitity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Time;

@Entity
@Table(name="tbl_shop_msg_history")
@Getter
@NoArgsConstructor( access = AccessLevel.PROTECTED)
public class ShopMessageHistory {

    @Id
    private int smhCode;
    private Integer userCode;
    private Integer shopCode;
    private Date date;
    private Time time;
    private String msgContent;
    private String msgType;

}
