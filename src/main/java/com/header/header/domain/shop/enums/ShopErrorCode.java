package com.header.header.domain.shop.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ShopErrorCode {

    // Shop CRUD 시 발생할 수 있는 에러 코드를 관리하는 enum

    NOT_SUPPORTED_REQUEST("NOT_SUPPORTED_REQUEST", "유효하지 않은 접근입니다."),
    SHOP_NOT_FOUND("SHOP_NOT_FOUND", "해당 샵을 찾을 수 없습니다."),
    SHOP_DEACTIVATED("SHOP_ALREADY_DEACTIVATED", "해당 샵은 비활성화 되었습니다."),
    LOCATION_NOT_FOUND("LOCATION_NOT_FOUND", "잘못된 주소입니다."),
    UNKNOWN_DB_VALUE("UNKNOWN_DB_VALUE", "유효하지 않은 값입니다."),
    ADMIN_NOT_FOUND("ADMIN_NOT_FOUND", "유효하지 않은 관리자 정보입니다.");

    private final String code;
    private final String message;
}
