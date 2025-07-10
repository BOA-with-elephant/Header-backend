package com.header.header.domain.reservation.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Date;
import java.sql.Time;

@Getter
@Setter
@ToString
public class BasicReservationDTO {

    private Integer resvCode;
    private Integer userCode;
    private Integer shopCode;
    private Integer menuCode;
    private Date resvDate;
    private Time resvTime;
    private String userComment;
    private String resvState;

    public BasicReservationDTO(){}

    public BasicReservationDTO(Integer resvCode, Integer userCode, Integer shopCode, Integer menuCode, Date resvDate, Time resvTime, String userComment, String resvState) {
        this.resvCode = resvCode;
        this.userCode = userCode;
        this.shopCode = shopCode;
        this.menuCode = menuCode;
        this.resvDate = resvDate;
        this.resvTime = resvTime;
        this.userComment = userComment;
        this.resvState = resvState;
    }
}
