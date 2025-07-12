package com.header.header.domain.shop.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShopSearchConditionDTO {
    private String keyword;
    private Integer categoryCode;
    private Double userLa; // 사용자 위도
    private Double userLong; // 사용자 경도
}
