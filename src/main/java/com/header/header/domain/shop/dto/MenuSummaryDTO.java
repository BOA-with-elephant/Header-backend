package com.header.header.domain.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class MenuSummaryDTO {
    private String menuName;
    private Integer menuRevCount;
}