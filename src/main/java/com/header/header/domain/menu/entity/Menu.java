package com.header.header.domain.menu.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="tbl_menu")
@Getter
@NoArgsConstructor( access = AccessLevel.PROTECTED)
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int menuCode;
    private Integer shopCode;
    private Integer categoryCode;
    private String menuName;
    private int menuPrice;
    private int estTime;
    private Boolean isActive;


}
