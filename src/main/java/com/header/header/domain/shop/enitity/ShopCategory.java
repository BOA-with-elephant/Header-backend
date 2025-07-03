package com.header.header.domain.shop.enitity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name="tbl_shop_category")
@Immutable  // Hibernate에게 읽기 전용임을 명시
@Getter
@NoArgsConstructor( access = AccessLevel.PROTECTED)
public class ShopCategory {

    @Id
    private int categoryCode;
    private String categoryName;
}
