package com.header.header.domain.shop.projection;

public interface ShopDetailResponse {
//사용자, 관리자의 샵 상세 조회용 프로젝션
    Integer getShopCode();
    String getShopName();
    String getShopPhone();
    String getShopLocation();
    String getShopOpen();
    String getShopClose();
    String getCategoryName();

    Integer getMenuCode();
    String getMenuName();
    Integer getMenuPrice();
    Integer getEstTime();
    String getMenuCategoryName();
}
