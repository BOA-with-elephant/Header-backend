package com.header.header.domain.shop.projection;

public interface ShopSummary {

    String getShopName();
    String getShopPhone();
    String getShopLocation();
    String getCategoryName();

    // 관리자의 요약 조회 데이터: shop - 샵 이름, 샵 전화번호, 샵 주소, category - 카테고리 이름
}
