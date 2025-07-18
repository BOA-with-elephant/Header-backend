package com.header.header.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Time;
import java.sql.Date;

@Getter
@Setter
public class UserReservationDTO {
    /*새로운 예약 생성시 사용되는 DTO*/

    private Integer userCode;

    private Integer menuCode;

    @FutureOrPresent(message = "예약은 오늘 이후의 날짜만 가능합니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date resvDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private Time resvTime;

    @Size(max = 255, message = "요청 사항은 최대 255자까지 입력 가능합니다")
    private String userComment;

    /*예약을 처음 생성할 때 예약 상태는 기본값(예약확정)으로 세팅되므로 필드 제외*/
}
