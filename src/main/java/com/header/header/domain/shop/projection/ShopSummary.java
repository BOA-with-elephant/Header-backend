package com.header.header.domain.shop.projection;

import com.header.header.domain.shop.enums.ShopStatus;

public interface ShopSummary {

    Integer getShopCode();
    Integer getCategoryCode();
    Integer getAdminCode();
    String getShopName();
    String getShopPhone();
    String getShopLocation();
    ShopStatus getShopStatus();
    Boolean getIsActive();
}
