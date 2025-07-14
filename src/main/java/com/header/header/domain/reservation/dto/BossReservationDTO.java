package com.header.header.domain.reservation.dto;

import com.header.header.domain.reservation.enums.ReservationState;
import com.header.header.domain.shop.dto.ShopDTO;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Date;
import java.sql.Time;

@Getter
@Setter
@ToString
public class BossReservationDTO {

    private Integer resvCode;
    private BossResvUserDTO userInfo;
    private Integer shopCode;
    private BossResvMenuDTO menuInfo;
    private Date resvDate;
    private Time resvTime;
    private String userComment;
    private ReservationState resvState;

    public BossReservationDTO() {
        this.userInfo = new BossResvUserDTO();  // 추가
        this.menuInfo = new BossResvMenuDTO();
    }


}
