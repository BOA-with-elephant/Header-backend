package com.header.header.domain.reservation.entity;

import com.header.header.domain.reservation.converter.UserReservationStateConverter;
import com.header.header.domain.reservation.enums.UserReservationErrorCode;
import com.header.header.domain.reservation.enums.UserReservationState;
import com.header.header.domain.reservation.exception.UserReservationExceptionHandler;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Immutable;

import java.sql.Date;
import java.sql.Time;

@Entity
@Table(name="tbl_reservation")
//@Immutable
// UPDATE 쿼리가 동작하지 않음. @Setter가 아닌 cancelReservation 비지니스 메소드를 사용해 보안 문제 해결
@Getter
@NoArgsConstructor( access = AccessLevel.PROTECTED)
@DynamicInsert //값이 null인 필드 자동 제외하여 default값 반영
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

    @Convert(converter = UserReservationStateConverter.class)
    private UserReservationState resvState;

    public void cancelReservation() {

        if (this.resvState == UserReservationState.CANCEL) {
            throw new UserReservationExceptionHandler(UserReservationErrorCode.RESV_ALREADY_DEACTIVATED);
        } else if (this.resvState == UserReservationState.FINISH) {
            throw new UserReservationExceptionHandler(UserReservationErrorCode.RESV_ALREADY_FINISHED);
        }

        this.resvState = UserReservationState.CANCEL;
    }

}
