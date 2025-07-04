package com.header.header.domain.menu.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="tbl_menu_category")
@Getter
@NoArgsConstructor( access = AccessLevel.PROTECTED)
public class MenuCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int categoryCode;
    private Integer shopCode;
    private String categoryName;
    private String menuColor;
    private Boolean isActive;

}
