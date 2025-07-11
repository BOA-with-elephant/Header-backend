package com.header.header.domain.reservation.entity;

import com.header.header.domain.reservation.converter.ReservationStateConverter;
import com.header.header.domain.reservation.dto.BasicReservationDTO;
import com.header.header.domain.reservation.enums.ReservationState;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Time;

@Entity
@Table(name="tbl_reservation")
@Getter
@NoArgsConstructor( access = AccessLevel.PROTECTED)
//@DynamicInsert //값이 null인 필드 자동 제외하여 default값 반영
public class Reservation {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    private Integer resvCode;
    private Integer userCode;
    private Integer shopCode;
    private Integer menuCode;
    private Date resvDate;
    private Time resvTime;
    private String userComment;

    @Convert(converter = ReservationStateConverter.class)
    private ReservationState resvState;

    public void cancelReservation() {

        this.resvState = ReservationState.CANCEL;
    }

    public void modifyReservation(BasicReservationDTO reservationDTO){
        this.menuCode = reservationDTO.getMenuCode();
        this.resvDate = reservationDTO.getResvDate();
        this.resvTime = reservationDTO.getResvTime();
        this.userComment = reservationDTO.getUserComment();
    }

}
