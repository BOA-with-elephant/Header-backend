package com.header.header.domain.reservation.projection;

import com.header.header.domain.reservation.enums.ReservationState;

import java.sql.Date;
import java.sql.Time;

public interface BossResvDetailView {

    Integer getResvCode();
    String getUserName();
    String getUserPhone();
    String getMenuColor();
    String getMenuName();
    Boolean getIsActive();  // 메뉴 활성화 여부
    ReservationState getResvState();
    Date getResvDate();
    Time getResvTime();
    String getUserComment();

}
