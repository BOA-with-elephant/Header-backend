package com.header.header.domain.shop.dto;

import jakarta.persistence.Enumerated;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ShopDTO {

    private Integer shopCode;
    private Integer categoryCode;
    private Integer adminCode;
    private String shopName;
    private String shopPhone;
    private String shopLocation;
    private Double shopLong;
    private Double shopLa;

    @Enumerated
    private String shopStatus;
    private String shopOpen;
    private String shopClose;
    private Boolean isActive;

}
