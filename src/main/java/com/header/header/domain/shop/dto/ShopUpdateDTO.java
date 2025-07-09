package com.header.header.domain.shop.dto;

import com.header.header.domain.shop.enums.ShopStatus;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ShopUpdateDTO {

    private String shopName;
    private String shopPhone;
    private String shopLocation;
    private ShopStatus shopStatus;
    private String shopOpen;
    private String shopClose;

}
