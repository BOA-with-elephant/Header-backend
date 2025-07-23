package com.header.header.domain.reservation.dto;

import com.header.header.domain.reservation.enums.ReservationState;
import lombok.*;

import java.sql.Date;
import java.sql.Time;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class BossResvProjectionDTO {

    private Integer resvCode;
    private String userName;
    private String userPhone;
    private String menuColor;
    private String menuName;
    private Boolean isActive;
    private ReservationState resvState;
    private Date resvDate;
    private Time resvTime;
    private String userComment;

    public BossResvProjectionDTO(Integer resvCode, String userName, String userPhone, String menuColor, String menuName, Boolean isActive, ReservationState resvState, Date resvDate, Time resvTime, String userComment) {
        this.resvCode = resvCode;
        this.userName = userName;
        this.userPhone = userPhone;
        this.menuColor = menuColor;
        this.menuName = menuName;
        this.isActive = isActive;
        this.resvState = resvState;
        this.resvDate = resvDate;
        this.resvTime = resvTime;
        this.userComment = userComment;
    }

    // Getter 생략 가능 (Lombok 쓰면 @Getter 추가)
}

