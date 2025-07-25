package com.header.header.domain.reservation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class ReservationDateAndTimeDTO {
    private LocalDate targetDate;
    private List<LocalTime> availableTimes;
    private List<LocalTime> reservedTimes;

    public ReservationDateAndTimeDTO(LocalDate targetDate, List<LocalTime> availableTimes, List<LocalTime> reservedTimes){
        this.targetDate = targetDate;
        this.availableTimes = availableTimes;
        this.reservedTimes = reservedTimes;
    }
}
