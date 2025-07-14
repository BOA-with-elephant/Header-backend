package com.header.header.domain.reservation.projection;

public interface UserReservationDetail {

    /*사용자가 예약 내역을 상세조회할 때 쓰이는 프로젝션
    * 조회 데이터:
    * Reservation => 예약 날짜, 예약 시간, 예약 상태
    * Shop => 샵 이름, 샵 주소
    * Menu => 메뉴 이름
    * */

    String getResvDate();
    String getResvTime();
    String getResvState();
    String getUserComment();
    String getShopName();
    String getShopLocation();
    String getMenuName();
    String getUserName();
    String getUserPhone();
}
