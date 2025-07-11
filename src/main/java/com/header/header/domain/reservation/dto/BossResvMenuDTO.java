package com.header.header.domain.reservation.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class BossResvMenuDTO {

    private Integer menuCode;
    private BossResvMenuCategoryDTO menuCategoryInfo;
    private String menuName;
    private int menuPrice;
    private int estTime;  //  예상 소요 시간
    private int isActive;  // 메뉴 활성 여부
}
