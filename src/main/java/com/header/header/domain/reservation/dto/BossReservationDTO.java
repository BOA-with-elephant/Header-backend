package com.header.header.domain.reservation.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Date;
import java.sql.Time;

@Getter
@Setter
@ToString
public class BossReservationDTO {

    private int resvCode;
    private BossResvUserDTO userInfo;
    private Integer shopCode;
    private BossResvMenuDTO menuInfo;
    private Date resvDate;
    private Time resvTime;
    private String userComment;
    private String resvState;

    public BossReservationDTO(){};

}
