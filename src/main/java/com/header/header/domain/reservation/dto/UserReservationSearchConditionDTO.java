package com.header.header.domain.reservation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Date;

@Getter
@Setter
public class UserReservationSearchConditionDTO {

    @NotBlank(message = "유효하지 않은 로그인 정보입니다.")
    Integer userCode;

    /*지정된 날짜 형식만 받을 수 있게 체크, null 허용*/
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    Date startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    Date endDate;

}
