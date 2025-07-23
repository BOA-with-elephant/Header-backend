package com.header.header.domain.shop.projection;

public interface ShopHolidayInfo {

    // 샵 별 휴일 정보를 리스트 형식으로 보여줄 때 사용

    Integer getShopCode();
    Integer getShopHolCode(); // 프론트에게 값 넘겨줄 때 필요
    String getHolStartDate();
    String getHolEndDate();
    Boolean getIsHolRepeat();

}
