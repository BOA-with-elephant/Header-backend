package com.header.header.domain.reservation.service;

import com.header.header.domain.shop.entity.ShopHoliday;
import com.header.header.domain.shop.repository.ShopHolidayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.DayOfWeek;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HolidayExaminationForCache {

    private final ShopHolidayRepository shopHolidayRepository;

    /*
    * 사용자가 접근하려는 날짜가 휴일인지 검증하는 메소드
    *
    * - 캐시 저장 (shopCode_날짜 : holidays)
    * */
    @Cacheable(value = "holidays", key = "#shopCode + '_' + #dateToScan.toString()")
    public boolean isHoliday(Integer shopCode, Date dateToScan) {

        /*임시 휴일 확인 - 단 하루만 검사하는 쿼리 */
        if (shopHolidayRepository.isTempHoliday(shopCode, dateToScan)) return true;

        /*정기 휴일 확인 - 스캔하려는 날짜보다 이전에 설정된 휴일이 있는지 확인 */
        List<ShopHoliday> repeatHols = shopHolidayRepository.findRegHoliday(shopCode, dateToScan);

        /* 반환된 값이 비어있지 않을 때만 검증 */
        if (!repeatHols.isEmpty()) {
            /*java.time의 요일 계산 클래스*/
            DayOfWeek day = dateToScan.toLocalDate().getDayOfWeek();
            for (ShopHoliday hol : repeatHols) {
                if (hol.getHolStartDate().toLocalDate().getDayOfWeek() == day) {
                    return true;
                }
            }
        }

        /* 휴일 아님 false 반환*/
        return false;
    }
}
