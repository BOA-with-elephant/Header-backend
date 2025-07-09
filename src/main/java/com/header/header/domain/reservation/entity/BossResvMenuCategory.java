package com.header.header.domain.reservation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="tbl_menu_category")
@Getter
@NoArgsConstructor( access = AccessLevel.PROTECTED)
public class BossResvMenuCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer categoryCode;
    @Id
    @GeneratedValue
    private Integer shopCode;
    private String categoryName;
    private String menuColor;
    private boolean isActive;

}
