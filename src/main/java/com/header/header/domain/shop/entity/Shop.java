package com.header.header.domain.shop.entity;

import com.header.header.domain.menu.entity.MenuCategory;
import com.header.header.domain.shop.converter.ShopStatusConverter;
import com.header.header.domain.shop.enums.ShopStatus;
import com.header.header.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name="tbl_shop")
@Getter
@NoArgsConstructor( access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer shopCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_code")
    private ShopCategory categoryInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_code")
    private User adminInfo;

    private String shopName;
    private String shopPhone;
    private String shopLocation;
    private Double shopLong;
    private Double shopLa;

    @Convert(converter = ShopStatusConverter.class)
    private ShopStatus shopStatus;

    private String shopOpen;
    private String shopClose;
    private Boolean isActive;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopCode")
    private List<MenuCategory> menuCategoryList;

    public void deactivateShop() {

        /* Shop을 논리적 삭제 (활성 상태 -> False 변환)하는 메소드
        *  Setter가 아니기 때문에 엔티티 내부에 직접 작성하였어도 보안 위협 낮음*/

        this.isActive = false;
    }

    public Shop(Integer shopCode){
        this.shopCode = shopCode;
    }

    public void updateShopInfo(
            ShopCategory categoryInfo, String shopName, String shopPhone,
            String shopLocation, Double shopLong, Double shopLa, ShopStatus shopStatus, String shopOpen, String shopClose) {
        this.categoryInfo = categoryInfo;
        this.shopName = shopName;
        this.shopPhone = shopPhone;
        this.shopLocation = shopLocation;
        this.shopLong = shopLong;
        this.shopLa = shopLa;
        this.shopStatus = shopStatus;
        this.shopOpen = shopOpen;
        this.shopClose = shopClose;
    }

}
