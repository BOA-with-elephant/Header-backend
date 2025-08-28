package com.header.header.domain.reservation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate targetDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private List<LocalTime> availableTimes;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private List<LocalTime> reservedTimes;

    public ReservationDateAndTimeDTO(LocalDate targetDate, List<LocalTime> availableTimes, List<LocalTime> reservedTimes){
        this.targetDate = targetDate;
        this.availableTimes = availableTimes;
        this.reservedTimes = reservedTimes;
    }
}
