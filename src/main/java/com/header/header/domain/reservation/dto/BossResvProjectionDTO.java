package com.header.header.domain.reservation.dto;

import com.header.header.domain.reservation.enums.ReservationState;
import lombok.*;

import java.sql.Date;
import java.sql.Time;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
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

}
