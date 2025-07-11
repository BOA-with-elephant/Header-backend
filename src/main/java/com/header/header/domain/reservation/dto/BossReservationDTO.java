package com.header.header.domain.reservation.dto;

import com.header.header.domain.shop.dto.ShopDTO;
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
    private ShopDTO shopCode;
    private BossResvMenuDTO menuInfo;
    private Date resvDate;
    private Time resvTime;
    private String userComment;
    private String resvState;

    public BossReservationDTO(){};

}
