package com.header.header.domain.shop.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.sql.Date;

@Setter
@Getter
public class CreateHolReqDTO {

    @NotBlank(message = "샵 정보가 유효하지 않습니다.")
    private Integer shopCode; //샵 코드가 휴일 생성할 때 꼭 필요한가?

    @NotBlank(message = "휴일 시작 날짜는 비워둘 수 없습니다.")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date endDate;

    @NotBlank
    boolean isHolRepeat;
}
