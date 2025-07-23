package com.header.header.domain.shop.exception;

import com.header.header.domain.shop.enums.ShopHolidayErrorCode;
import lombok.Getter;

@Getter
public class ShopHolidayExceptionHandler extends RuntimeException {
    private final ShopHolidayErrorCode shopHolidayErrorCode;

    public ShopHolidayExceptionHandler(ShopHolidayErrorCode shopHolidayErrorCode) {
        super(shopHolidayErrorCode.getMessage());
        this.shopHolidayErrorCode = shopHolidayErrorCode;
    }

    public ShopHolidayExceptionHandler(ShopHolidayErrorCode shopHolidayErrorCode, String message) {
        super(message);
        this.shopHolidayErrorCode = shopHolidayErrorCode;
    }
}
