package com.header.header.domain.reservation.projection;

public interface UserReservationSummary {

    /*사용자가 자신이 예약한 다수의 예약들을 요약 조회하는 프로젝션*/

    String getResvDate();
    String getResvTime();
    String getResvState();
    String getShopName();
    String getShopLocation();
    String getMenuName();

}
