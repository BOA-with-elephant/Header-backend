package com.header.header.domain.shop.exception;

import com.header.header.domain.shop.enums.ShopErrorCode;
import lombok.Getter;

@Getter
public class ShopExceptionHandler extends RuntimeException {

    // enum으로 관리되는 ShopErrorCode를 실제로 사용하기 위한 Exception 클래스
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
