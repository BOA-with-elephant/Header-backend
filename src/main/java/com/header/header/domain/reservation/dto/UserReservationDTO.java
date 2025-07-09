package com.header.header.domain.reservation.dto;

import com.header.header.domain.reservation.enums.UserReservationState;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.sql.Date;
import java.sql.Time;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class UserReservationDTO {

    private Integer resvCode;
    private Integer userCode;
    private Integer shopCode;

    @NotBlank(message = "시술 메뉴 선택은 필수입니다")
    private Integer menuCode;

    private Date resvDate;
    private Time resvTime;

    @Size(max = 255, message = "요청 사항은 최대 255자까지 입력 가능합니다")
    private String userComment;
    private UserReservationState resvState;
}
