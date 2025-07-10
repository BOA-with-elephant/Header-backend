package com.header.header.domain.shop.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ShopStatus {

    //ShopStatus(샵 상태)를 관리하는 enum

    OPEN("영업중"),
    CLOSED("휴업중");

    private final String dbName;
}
