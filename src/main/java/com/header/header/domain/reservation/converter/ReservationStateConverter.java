package com.header.header.domain.reservation.converter;

import com.header.header.domain.reservation.enums.ReservationState;
import com.header.header.domain.shop.enums.ShopErrorCode;
import com.header.header.domain.shop.exception.ShopExceptionHandler;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ReservationStateConverter implements AttributeConverter<ReservationState, String> {
    @Override
    public String convertToDatabaseColumn(ReservationState reservationState) {
        return reservationState != null ? reservationState.getDbName() : null;
    }

    @Override
    public ReservationState convertToEntityAttribute(String dbName) {
        if (dbName == null) {
            return null;
        }

        for (ReservationState state : ReservationState.values()) {
            if (state.getDbName().equals(dbName)) {
                return state;
            }
        }

        throw new ShopExceptionHandler(ShopErrorCode.UNKNOWN_DB_VALUE);
    }
}
