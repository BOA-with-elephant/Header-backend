package com.header.header.domain.reservation.projection;

import com.header.header.domain.reservation.enums.ReservationState;

public interface UserReservationSummary {

    Integer getResvCode();
    Integer getUserCode();
    Integer getShopCode();
    String getResvDate();
    ReservationState getResvState();

}
