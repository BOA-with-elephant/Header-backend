package com.header.header.domain.shop.repository;

import com.header.header.domain.shop.entity.ShopHoliday;
import com.header.header.domain.shop.projection.ShopHolidayInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Date;
import java.util.List;

public interface ShopHolidayRepository extends JpaRepository<ShopHoliday, Integer> {

    /*특정 가게에 설정된 모든 휴일 조회*//*
    List<ShopHoliday> findByShopCode(Integer shopCode);*/

    /*특정 날짜가 일시 휴일인지 확인하는 검증용 쿼리
    * 입력한 날짜가 해당 샵의 holStartDate 부터 holEndDate 사이에 있으면 true 반환
    * */
    @Query("""
            SELECT COUNT(h) > 0
            FROM ShopHoliday h 
            WHERE h.shopInfo.shopCode = :shopCode
            AND h.isHolRepeat = false 
            AND :dateToScan BETWEEN h.holStartDate AND h.holEndDate
           """)
    boolean isTempHoliday (@Param("shopCode") Integer shopCode, @Param("dateToScan") Date dateToScan);


    /*특정 날짜가 정기 휴일에 포함될 수 있는지 확인하기 위한 쿼리
      1) isHolRepeat이 true 이면서 사용자가 입력한 값보다 작은 holStartDate 가 있는 데이터 조회
      2) 서비스단에서 값이 있는 경우, DayOfWeek(java.time) 메소드 활용하여 요일 확인, 휴일인지 검증 */
    @Query("""
            SELECT h
            FROM ShopHoliday h 
            WHERE h.shopInfo.shopCode = :shopCode
            AND h.isHolRepeat = true 
            AND :dateToScan >= h.holStartDate      
        """)
    List<ShopHoliday> findRegHoliday (@Param("shopCode") Integer shopCode, @Param("dateToScan") Date dateToScan);

    /*각각의 샵이 가진 휴일 정보를 불러옴
    * 데이터: 휴일 시작일, 휴일 종료일, 반복 여부 (단기 휴일인지 정기 휴일인지 판단)
    * 정렬: 휴일 시작 날짜 오름 차순
    * */
    @Query("""
           SELECT 
            h.shopInfo.shopCode as shopCode,
            h.holStartDate AS holStartDate,
            h.holEndDate AS holEndDate,
            h.isHolRepeat AS isHolRepeat
            FROM ShopHoliday h
            WHERE (h.shopInfo.shopCode = :shopCode AND h.holEndDate >= :today) 
               OR (h.shopInfo.shopCode = :shopCode AND h.holEndDate IS NULL)
            ORDER BY h.holStartDate ASC 
            """)
    List<ShopHolidayInfo> getShopHolidayInfo(@Param("shopCode") Integer shopCode, @Param("today") Date today);

    // 그 샵에 해당하는 휴일 정보가 맞는지 검증 (휴일 삭제시 사용)
    @Query("""
           SELECT COUNT(h) = 1 
           FROM ShopHoliday h
           WHERE h.shopInfo.shopCode = :shopCode
           AND h.shopHolCode = :shopHolCode
           """)
    boolean isHolReal(@Param("shopCode") Integer shopCode, @Param("shopHolCode") Integer shopHolCode);
}
