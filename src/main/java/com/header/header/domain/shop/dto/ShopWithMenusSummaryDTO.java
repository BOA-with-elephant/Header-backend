package com.header.header.domain.shop.dto;

import java.util.List;

public interface ShopWithMenusSummaryDTO {
    Integer getShopCode();
    String getShopName();
    String getShopPhone();
    String getShopLocation();
    Double getShopLong();
    Double getShopLa();
    String getCategoryName();
    Double getDistance();
    String getMenuName();
    Integer getMenuRevCount();

    List<MenuSummaryDTO> getMenus();
}
