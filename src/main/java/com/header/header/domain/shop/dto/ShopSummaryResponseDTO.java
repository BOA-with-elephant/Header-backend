package com.header.header.domain.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ShopSummaryResponseDTO {

    private String shopName;
    private String shopPhone;
    private String shopLocation;
    private String categoryName;
    private Double distance; // 사용자 기준 거리 (프론트에 전달)

    //조회 데이터: Shop - 샵 이름, 전화번호, 주소/ CategoryCode - 카테고리 이름 (예: 미용실)

}
