package com.header.header.domain.shop.exception;

public class ShopAlreadyDeletedException extends RuntimeException {
    public ShopAlreadyDeletedException(String message) {
        super(message);
    }
}
