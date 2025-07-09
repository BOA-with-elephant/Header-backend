package com.header.header.domain.shop.converter;

import com.header.header.domain.shop.enums.ShopErrorCode;
import com.header.header.domain.shop.enums.ShopStatus;
import com.header.header.domain.shop.exception.ShopExceptionHandler;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/* 코드 작성시 영어로 하고, 실제로 저장되는 이름(dbName)은 한글로 들어갈 수 있도록 하는 Converter
*  변경 대상은 ShopStatus (enum) */
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