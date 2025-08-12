package com.header.header.domain.shop.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ShopWithMenusSummaryDTO {
    private Integer shopCode;
    private String shopName;
    private String shopPhone;
    private String shopLocation;
    private Double shopLong;
    private Double shopLa;
    private String categoryName;
    private Double distance;

    private List<MenuSummaryDTO> menus;

    public ShopWithMenusSummaryDTO(Integer shopCode, String shopName, String shopPhone, String shopLocation, Double shopLong, Double shopLa, String categoryName, Double distance) {
        this.shopCode = shopCode;
        this.shopName = shopName;
        this.shopPhone = shopPhone;
        this.shopLocation = shopLocation;
        this.shopLong = shopLong;
        this.shopLa = shopLa;
        this.categoryName = categoryName;
        this.distance = distance;
    }
}
