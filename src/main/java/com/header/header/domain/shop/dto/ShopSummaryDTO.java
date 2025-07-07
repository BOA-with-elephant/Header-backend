package com.header.header.domain.shop.dto;

import com.header.header.domain.shop.enums.ShopStatus;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ShopSummaryDTO {

    private Integer shopCode;
    private Integer categoryCode;
    private Integer adminCode;
    private String shopName;
    private String shopLocation;
    private ShopStatus shopStatus;
}
