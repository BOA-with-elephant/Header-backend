package com.header.header.domain.shop.enums;

import com.header.header.domain.shop.exception.ShopExceptionHandler;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ShopStatus {

    //ShopStatus(샵 상태)를 관리하는 enum

    OPEN("영업중"),
    CLOSED("휴업중");

    private final String dbName;

    public static ShopStatus fromDbName(String dbName) {
        for (ShopStatus status : ShopStatus.values()) {
            if (status.getDbName().equals(dbName)) {
                return status;
            }
        }
        throw new ShopExceptionHandler(ShopErrorCode.UNKNOWN_DB_VALUE);
    }
}
