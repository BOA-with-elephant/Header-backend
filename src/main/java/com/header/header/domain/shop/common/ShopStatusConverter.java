package com.header.header.domain.shop.common;

import com.header.header.domain.shop.enums.ShopErrorCode;
import com.header.header.domain.shop.enums.ShopStatus;
import com.header.header.domain.shop.exception.ShopExceptionHandler;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ShopStatusConverter implements AttributeConverter<ShopStatus, String> {

    @Override
    public String convertToDatabaseColumn(ShopStatus shopStatus) {
        return shopStatus != null ? shopStatus.getDbName() : null;
    }

    @Override
    public ShopStatus convertToEntityAttribute(String dbName) {
        if (dbName == null) {
            return null;
        }
        
        for (ShopStatus status : ShopStatus.values()) {
            if (status.getDbName().equals(dbName)) {
                return status;
            }
        }
        
        throw new ShopExceptionHandler(ShopErrorCode.UNKNOWN_DB_VALUE);
    }
}