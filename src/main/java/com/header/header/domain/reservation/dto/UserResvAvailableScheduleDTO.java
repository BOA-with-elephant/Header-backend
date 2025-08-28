package com.header.header.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
// 가능한 시간을 생성자 형태로 담아주기 위해 사용
@AllArgsConstructor
public class UserResvAvailableScheduleDTO {
    /* 프론트에 가능한 스케쥴만 스캔하여 보내주는 DTO */

    // 한 날짜는 여러개의 가능한 시간이 존재함

    /* 날짜만 다뤄야 하기 때문에, 혹시 모를 문제점을 방지하기 위해 LocalDate 사용
    *  + LocalDate는 불변 객체로 스레드 안전성을 보장한다 */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate targetDate;

    // 현재 시간대 정보 필요해서 LocalTime 사용
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private List<LocalTime> availableTimes;
}
