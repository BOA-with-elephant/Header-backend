package com.header.header.domain.reservation.projection;

/*
* LLM 기능에서 챗봇에게 유저가 예약한 내역을 학습시킴
* - 샵 정보 (코드, 이름)
* - 메뉴 정보(코드, 이름)
* - 샵의 해당 메뉴에 대한 회원의 예약 횟수
* */
public interface UserReservationForLLM {
    Integer getShopCode();
    String getShopName();
    Integer getMenuCode();
    Integer getMenuCategoryCode();
    String getMenuName();
    Integer getRevCount();
}
