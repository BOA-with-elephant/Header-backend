package com.header.header.domain.reservation.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;

import java.time.LocalDate;


@Getter
@Setter
@AllArgsConstructor
@ToString
@NoArgsConstructor // 테스트에 사용
public class UserReservationSearchConditionDTO {

    Integer userCode;

    /*지정된 날짜 형식만 받을 수 있게 체크, null 허용*/
    @Nullable
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate startDate;

    @Nullable
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate endDate;

}
