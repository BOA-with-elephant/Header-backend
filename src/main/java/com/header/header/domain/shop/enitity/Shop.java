package com.header.header.domain.shop.enitity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="tbl_shop")
@Getter
@NoArgsConstructor( access = AccessLevel.PROTECTED)
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int shopCode;
    private Integer categoryCode;
    private Integer adminCode;
    private String shopName;
    private String shopPhone;
    private String shopLocation;
    private Double shopLong;
    private Double shopLa;
    private String shopStatus;
    private String shopOpen;
    private String shopClose;
    private Boolean isActive;


}
