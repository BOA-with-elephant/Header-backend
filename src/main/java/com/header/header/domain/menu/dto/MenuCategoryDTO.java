package com.header.header.domain.menu.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MenuCategoryDTO {

    private Integer categoryCode;
    private Integer shopCode;
    private String categoryName;
    private String menuColor;
    private Boolean isActive;
}
