package com.header.header.domain.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MenuSummaryDTO {
    private Integer shopCode;
    private Integer menuCode;
    private String menuName;
    private Integer menuRevCount;
}