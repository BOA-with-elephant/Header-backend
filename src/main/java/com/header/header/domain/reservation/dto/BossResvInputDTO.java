package com.header.header.domain.reservation.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Date;
import java.sql.Time;

@Getter
@Setter
@ToString
public class BossResvInputDTO {

    private String userName;
    private String userPhone;
    private Integer shopCode;
    private String menuName;
    private Date resvDate;
    private Time resvTime;
    private String userComment;

    public void BossResvInputDTO() {
    }

    public BossResvInputDTO(String menuName, Date resvDate, Time resvTime, String userComment) {
        this.menuName = menuName;
        this.resvDate = resvDate;
        this.resvTime = resvTime;
        this.userComment = userComment;
    }

    public BossResvInputDTO(String userName, String userPhone, Integer shopCode, String menuName, Date resvDate, Time resvTime, String userComment) {
        this(menuName, resvDate, resvTime, userComment);
        this.userName = userName;
        this.userPhone = userPhone;
        this.shopCode = shopCode;
    }
}
