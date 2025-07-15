package com.header.header.domain.shop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;

@Entity
@Table(name="tbl_shop_holiday")
@Getter
@NoArgsConstructor( access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ShopHoliday {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer shopHolCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_code")
    private Shop shopInfo;

    private Date holStartDate; // not null
    private Date holEndDate; //nullable

    private Boolean isHolRepeat;
    /* 휴일 반복 여부
       1) true: startDate + 7 한 값을 곱해줌, endDate null 허용
       2) false: endDate null 불허 */

    public void updateHolidayInfo(
            Date startDate, Date endDate, Boolean isHolRepeat
    ) {
        this.holStartDate = startDate;
        this.holEndDate = endDate;
        this.isHolRepeat = isHolRepeat;
    }

}
