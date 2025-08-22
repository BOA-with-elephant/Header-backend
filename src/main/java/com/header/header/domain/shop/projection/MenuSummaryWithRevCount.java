package com.header.header.domain.shop.projection;

public interface MenuSummaryWithRevCount {
    Integer getShopCode();
    Integer getMenuCode();
    Integer getMenuCategoryCode();
    String getMenuName();
    Integer getMenuRevCount();
}