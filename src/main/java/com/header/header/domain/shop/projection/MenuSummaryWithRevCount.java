package com.header.header.domain.shop.projection;

public interface MenuSummaryWithRevCount {
    Integer getShopCode();
    Integer getMenuCode();
    String getMenuName();
    Integer getMenuRevCount();
}