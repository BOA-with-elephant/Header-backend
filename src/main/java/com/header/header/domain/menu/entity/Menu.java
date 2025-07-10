package com.header.header.domain.menu.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "tbl_menu")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@ToString
public class Menu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer menuCode;
    private String menuName;
    private Integer menuPrice;
    private Integer estTime;
    private Boolean isActive;

    /**
     * 메뉴카테고리와의 연관관계
     *
     * @JoinColumns: 복합키를 참조하기 위해 2개 컬럼 조인
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "category_code", referencedColumnName = "categoryCode"),
        @JoinColumn(name = "shop_code", referencedColumnName = "shopCode")
    })
    @ToString.Exclude   // 연관 엔티티 무한 순환 방지
    private MenuCategory menuCategory;

    // 업데이트 메소드들
    public void updateMenuInfo(String menuName, Integer menuPrice, Integer estTime) {
        this.menuName = menuName;
        this.menuPrice = menuPrice;
        this.estTime = estTime;
    }

    public void updateActiveStatus(Boolean isActive) {
        this.isActive = isActive;
    }

}
