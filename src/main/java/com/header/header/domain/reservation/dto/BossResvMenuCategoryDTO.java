package com.header.header.domain.reservation.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BossResvMenuCategoryDTO {

    private Integer categoryCode;
    private String categoryName;
    private String menuColor;
    private Boolean isActive;
}
