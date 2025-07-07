package com.header.header.domain.shop.dto;

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
    private String shopStatus;
    private String shopOpen;
    private String shopClose;

}
