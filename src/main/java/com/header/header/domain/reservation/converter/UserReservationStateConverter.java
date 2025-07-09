package com.header.header.domain.reservation.converter;

import com.header.header.domain.reservation.enums.UserReservationState;
import com.header.header.domain.shop.enums.ShopErrorCode;
import com.header.header.domain.shop.exception.ShopExceptionHandler;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class UserReservationStateConverter implements AttributeConverter<UserReservationState, String> {
    @Override
    public String convertToDatabaseColumn(UserReservationState userReservationState) {
        return userReservationState != null ? userReservationState.getDbName() : null;
    }

    @Override
    public UserReservationState convertToEntityAttribute(String dbName) {
        if (dbName == null) {
            return null;
        }

        for (UserReservationState state : UserReservationState.values()) {
            if (state.getDbName().equals(dbName)) {
                return state;
            }
        }

        throw new ShopExceptionHandler(ShopErrorCode.UNKNOWN_DB_VALUE);
    }
}
