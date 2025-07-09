package com.header.header.domain.reservation.dto;

import com.header.header.domain.reservation.enums.UserReservationState;
import jakarta.transaction.Transactional;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
public class UserReservationSummaryDTO {
    //view 전용

    private Integer resvCode;
    private Integer userCode;
    private Integer shopCode;
    private Date resvDate;
    private UserReservationState resvState;

}
