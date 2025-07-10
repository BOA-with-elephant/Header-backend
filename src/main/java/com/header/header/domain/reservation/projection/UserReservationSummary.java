package com.header.header.domain.reservation.projection;

import com.header.header.domain.reservation.enums.UserReservationState;

public interface UserReservationSummary {

    Integer getResvCode();
    Integer getUserCode();
    Integer getShopCode();
    String getResvDate();
    UserReservationState getResvState();

}
