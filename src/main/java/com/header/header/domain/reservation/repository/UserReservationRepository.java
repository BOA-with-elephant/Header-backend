package com.header.header.domain.reservation.repository;

import com.header.header.domain.reservation.entity.BossReservation;
import com.header.header.domain.reservation.enums.ReservationState;
import com.header.header.domain.reservation.projection.UserReservationDetail;
import com.header.header.domain.reservation.projection.UserReservationSummary;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserReservationRepository extends JpaRepository<BossReservation, Integer> {

    /*사용자가 특정 예약에 대한 내역을 상세 조회할 경우
     * 매개변수 : userCode, resvCode
     * 조회 데이터:
     *           예약 날짜/시간/상태/요청사항(코멘트)
     *           샵  이름/주소
     *           메뉴 이름
     *           유저 이름/전화번호
     * */
    @Query("""
                SELECT 
                        r.resvDate AS resvDate,
                        r.resvTime AS resvTime,
                        r.resvState AS resvState,
                        r.userComment AS userComment,
                        s.shopName AS shopName,
                        s.shopLocation AS shopLocation,
                        m.menuName AS menuName,
                        u.userName AS userName,
                        u.userPhone AS userPhone
                FROM BossReservation r
                JOIN Shop s ON r.shopInfo.shopCode = s.shopCode
                JOIN Menu m ON r.menuInfo.menuCode = m.menuCode
                JOIN User u ON r.userInfo.userCode = u.userCode
                WHERE u.userCode = :userCode 
                    AND r.resvCode = :resvCode
            """)
    Optional<UserReservationDetail> readDetailByUserCodeAndResvCode(Integer userCode, Integer resvCode);

    /*사용자가 자신이 예약한 내역들의 목록을 조회할 경우
     * 매개변수 : userCode
     * 조회 데이터:
     *           예약 날짜/시간/상태
     *           샵  이름/주소
     *           메뉴 이름
     * 데이터 정렬: 최근 날짜 순
     * */

    @Query("""
                SELECT 
                    r.resvDate AS resvDate,
                    r.resvTime AS resvTime,
                    r.resvState AS resvState,
                    s.shopName AS shopName,
                    s.shopLocation AS shopLocation,
                    m.menuName AS menuName
                FROM BossReservation r
                JOIN Shop s ON r.shopInfo.shopCode = s.shopCode
                JOIN Menu m ON r.menuInfo.menuCode = m.menuCode
                WHERE r.userInfo.userCode = :userCode
                    AND (:startDate IS NULL OR r.resvDate >= :startDate)
                    AND (:endDate IS NULL OR r.resvDate <= :endDate)
                ORDER BY r.resvDate DESC
            """)
    List<UserReservationSummary> findResvSummaryByUserCode(
            @Param("userCode") Integer userCode,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    /* 휴일 날짜가 이미 예약 상에 존재하는지 검증하는 용도 (일시적 휴일)*/
    @Query("""
            SELECT COUNT (r) > 0
            FROM BossReservation r
            WHERE r.shopInfo.shopCode = :shopCode
            AND r.resvDate BETWEEN :startDate AND :endDate
            AND NOT r.resvState = 'CANCEL'
            """)
    boolean isExistDateBetweenHols(
            @Param("shopCode") Integer shopCode,
            @Param("startDate") Date startDate,
            @Param("endDate") Date endDate
    );

    /* 휴일 날짜가 이미 예약 상에 존재하는지 검증하는 용도 (반복적 휴일, 반복문으로 검증)*/
    @Query("""
           SELECT COUNT(r) > 0
           FROM BossReservation r
           WHERE r.shopInfo.shopCode = :shopCode
           AND r.resvDate = :resvDate
           AND NOT r.resvState = 'CANCEL'
           """)
    boolean isExistRepeatHols(
            @Param("shopCode") Integer shopCode,
            @Param("resvDate") Date resvDate
    );


}
