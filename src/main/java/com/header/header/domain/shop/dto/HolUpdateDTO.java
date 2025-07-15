package com.header.header.domain.shop.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Date;

@Getter
@Setter
@Builder //test
public class HolUpdateDTO {
    /*임시 휴일 업데이트*/

    // Creation + shopHolCode
    @NotBlank(message = "샵 휴일 정보가 유효하지 않습니다.")
    private Integer shopHolCode;

    @NotBlank(message = "샵 정보가 유효하지 않습니다.")
    private Integer shopCode;

    @NotBlank(message = "휴일 시작 날짜는 비워둘 수 없습니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @FutureOrPresent(message = "과거 날짜는 설정할 수 없습니다.")
    private Date startDate;

    @NotBlank(message = "휴일 종료 날짜는 비워둘 수 없습니다.")
    @FutureOrPresent(message = "과거 날짜는 설정할 수 없습니다.")
    private Date endDate;

    @NotBlank
    private Boolean isHolRepeat;

}
