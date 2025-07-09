package com.header.header.domain.reservation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="tbl_menu")
@Getter
@NoArgsConstructor( access = AccessLevel.PROTECTED)
public class BossResvMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int menuCode;
    @ManyToOne(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "categoryCode", referencedColumnName = "categoryCode"),
            @JoinColumn(name = "shopCode", referencedColumnName = "shopCode")
    })
    private BossResvMenuCategory menuCategoryInfo;
    private String menuName;
    private int menuPrice;
    private int estTime;
    private boolean isActive;
}
