package com.header.header.domain.shop.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ShopStatus {

    OPEN("영업중"),     //영업중
    CLOSED("휴업중");

    private final String dbName;
}
