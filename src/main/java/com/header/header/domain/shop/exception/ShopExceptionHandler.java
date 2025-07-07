package com.header.header.domain.shop.exception;

import com.header.header.domain.shop.enums.ShopErrorCode;
import lombok.Getter;

@Getter
public class ShopExceptionHandler extends RuntimeException {
    private final ShopErrorCode shopErrorCode;

    public ShopExceptionHandler(ShopErrorCode shopErrorCode) {
        super(shopErrorCode.getMessage());
        this.shopErrorCode = shopErrorCode;
    }

    public ShopExceptionHandler(ShopErrorCode shopErrorCode, String message) {
      super(message);
      this.shopErrorCode = shopErrorCode;
    }
}
