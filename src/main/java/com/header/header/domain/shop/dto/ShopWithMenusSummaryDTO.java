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
    private String menuName;
    private Integer menuRevCount;

    private List<MenuSummaryDTO> menus;
}
