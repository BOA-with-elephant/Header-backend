package com.header.header.domain.reservation.dto;

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

    @NotBlank(message = "유효하지 않은 로그인 정보입니다.")
    private Integer userCode;

    @NotBlank(message = "유효하지 않은 샵 정보입니다.")
    private Integer shopCode;

    @NotBlank(message = "시술 메뉴 선택은 필수입니다")
    private Integer menuCode;

    @NotBlank(message = "예약 날짜는 필수입니다.")
    @FutureOrPresent(message = "예약은 오늘 이후의 날짜만 가능합니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date resvDate;

    @NotBlank(message = "예약 시간은 필수입니다.")
    @DateTimeFormat(pattern = "HH:mm")
    private Time resvTime;

    @Size(max = 255, message = "요청 사항은 최대 255자까지 입력 가능합니다")
    private String userComment;

    /*예약을 처음 생성할 때 예약 상태는 기본값(예약확정)으로 세팅되므로 필드 제외*/
}
