package com.header.header.domain.shop.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Date;

@Setter
@Getter
@ToString //테스트용
public class HolResDTO {
    private Integer shopHolCode;
    private Integer shopCode;
    private Date holStartDate;
    private Date holEndDate;
    private Boolean isHolRepeat;

    private String description; // 안내문구 예) "매주 {weekOfDay} 휴일이 설정되었습니다 ???
}
