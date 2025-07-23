package com.header.header.domain.shop.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Date;

@Setter
@Getter
public class HolCreationDTO {
    // 휴일 생성시 사용하는 DTO

    @NotBlank(message = "휴일 시작 날짜는 비워둘 수 없습니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "과거 날짜는 설정할 수 없습니다.")
    private Date startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "과거 날짜는 설정할 수 없습니다.")
    private Date endDate;

    @NotBlank
    private Boolean isHolRepeat;
}
