package com.header.header.domain.shop.entity;

import com.header.header.domain.shop.converter.ShopStatusConverter;
import com.header.header.domain.shop.enums.ShopErrorCode;
import com.header.header.domain.shop.enums.ShopStatus;
import com.header.header.domain.shop.exception.ShopExceptionHandler;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name="tbl_shop")
@Getter
@NoArgsConstructor( access = AccessLevel.PROTECTED)
@DynamicInsert //쿼리를 실행할 때 값이 null인 필드 자동 제외하여 default값 반영
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer shopCode;
    private Integer categoryCode;
    private Integer adminCode;
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

    public void deactivateShop() {

        /* Shop을 논리적 삭제 (활성 상태 -> False 변환)하는 메소드
        *  Setter가 아니기 때문에 엔티티 내부에 직접 작성하였어도 보안 위협 낮음*/

        this.isActive = false;
    }

}
