package com.header.header.domain.reservation.repository;

import com.header.header.domain.reservation.entity.BossReservation;
import com.header.header.domain.reservation.enums.ReservationState;
import com.header.header.domain.reservation.projection.BossResvDetailView;
import jakarta.persistence.LockModeType;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.Optional;

public interface BossReservationRepository extends JpaRepository<BossReservation, Integer> {

    @Query("""
        SELECT new com.header.header.domain.reservation.dto.BossResvProjectionDTO(
            r.resvCode, u.userName, u.userPhone, mc.menuColor, m.menuName, m.isActive,
            r.resvState, r.resvDate, r.resvTime, r.userComment
        )
        FROM BossReservation r
        JOIN r.userInfo u
        JOIN r.shopInfo s
        JOIN r.menuInfo m
        JOIN m.menuCategory mc
        WHERE s.shopCode = :shopCode
          AND r.resvDate >= :startDate
          AND r.resvDate <= :endDate
    """)
    List<BossResvDetailView> findByShopCodeAndResvMonth(@Param("shopCode")Integer shopCode, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    /* 선택된 날짜의 예약 조회 */
    @Query("""
        SELECT new com.header.header.domain.reservation.dto.BossResvProjectionDTO(
            r.resvCode, u.userName, u.userPhone, mc.menuColor, m.menuName, m.isActive,
            r.resvState, r.resvDate, r.resvTime, r.userComment
        )
        FROM BossReservation r
        JOIN r.userInfo u
        JOIN r.shopInfo s
        JOIN r.menuInfo m
        JOIN m.menuCategory mc
        WHERE s.shopCode = :shopCode
        AND r.resvDate = :selectedDate
    """)
    List<BossResvDetailView> findByShopCodeAndResvDate(@Param("shopCode") Integer shopCode, @Param("selectedDate") Date selectedDate);

    /* 고객 이름으로 예약 조회 */
    @Query("""
        SELECT new com.header.header.domain.reservation.dto.BossResvProjectionDTO(
            r.resvCode, u.userName, u.userPhone, mc.menuColor, m.menuName, m.isActive,
            r.resvState, r.resvDate, r.resvTime, r.userComment
        )
        FROM BossReservation r
        JOIN r.userInfo u
        JOIN r.shopInfo s
        JOIN r.menuInfo m
        JOIN m.menuCategory mc
        WHERE s.shopCode = :shopCode
        AND u.userName LIKE CONCAT('%', :userName, '%')
    """)
    List<BossResvDetailView> findByShopCodeAndUserName(@Param("shopCode") Integer shopCode, @Param("userName") String userName);

    /* 예약 번호로 예약 상세조회 */
    @Query("""
        SELECT new com.header.header.domain.reservation.dto.BossResvProjectionDTO(
            r.resvCode, u.userName, u.userPhone, mc.menuColor, m.menuName, m.isActive,
            r.resvState, r.resvDate, r.resvTime, r.userComment
        )
        FROM BossReservation r
        JOIN r.userInfo u
        JOIN r.shopInfo s
        JOIN r.menuInfo m
        JOIN m.menuCategory mc 
        WHERE r.resvCode = :resvCode
    """)
    Optional<BossResvDetailView> findByResvCode(@Param("resvCode") Integer resvCode);

    /* 메뉴 이름 별 예약 내역 조회 */
    @Query("""
        SELECT new com.header.header.domain.reservation.dto.BossResvProjectionDTO(
            r.resvCode, u.userName, u.userPhone, mc.menuColor, m.menuName, m.isActive,
            r.resvState, r.resvDate, r.resvTime, r.userComment
        )
        FROM BossReservation r
        JOIN r.userInfo u
        JOIN r.shopInfo s
        JOIN r.menuInfo m
        JOIN m.menuCategory mc 
        WHERE s.shopCode = :shopCode 
        AND m.menuName LIKE CONCAT('%', :menuName, '%')
    """)
    List<BossResvDetailView> findByShopCodeAndMenuName(@Param("shopCode") Integer shopCode, @Param("menuName") String menuName);

    /* 노쇼 이력 조회 -> resvDate가 오늘 이전인 경우이면서 resvState가 "예약 확정"인 것 */
    @Query("""
        SELECT new com.header.header.domain.reservation.dto.BossResvProjectionDTO(
            r.resvCode, u.userName, u.userPhone, mc.menuColor, m.menuName, m.isActive,
            r.resvState, r.resvDate, r.resvTime, r.userComment
        )
        FROM BossReservation r
        JOIN r.userInfo u
        JOIN r.shopInfo s
        JOIN r.menuInfo m
        JOIN m.menuCategory mc 
        WHERE r.resvDate < :today 
        AND r.resvState = :resvState
        AND s.shopCode = :shopCode
        ORDER BY r.resvDate, r.resvTime
    """)
    List<BossResvDetailView> findByResvDateAndResvState(@Param("today") Date today, @Param("resvState") ReservationState resvState, @Param("shopCode") Integer shopCode);

    /* 테스트용 */
    @Query("""
        SELECT new com.header.header.domain.reservation.dto.BossResvProjectionDTO(
            r.resvCode, u.userName, u.userPhone, mc.menuColor, m.menuName, m.isActive,
            r.resvState, r.resvDate, r.resvTime, r.userComment
        )
        FROM BossReservation r
        JOIN r.userInfo u
        JOIN r.shopInfo s
        JOIN r.menuInfo m
        JOIN m.menuCategory mc  WHERE s.shopCode = :shopCode AND u.userName = :userName AND u.userPhone = :userPhone
    """)
    List<BossResvDetailView> findByUserNameAndUserPhone(@Param("shopCode") Integer shopCode, @Param("userName") String userName, @Param("userPhone") String userPhone);

    /* comment. 노쇼 & 취소 조회 */
    @Query("""
        SELECT new com.header.header.domain.reservation.dto.BossResvProjectionDTO(
            r.resvCode, u.userName, u.userPhone, mc.menuColor, m.menuName, m.isActive,
            r.resvState, r.resvDate, r.resvTime, r.userComment
        )
        FROM BossReservation r
        JOIN r.userInfo u
        JOIN r.shopInfo s
        JOIN r.menuInfo m
        JOIN m.menuCategory mc  
        WHERE s.shopCode = :shopCode 
         AND r.resvState = "예약취소"
        ORDER BY r.resvDate, r.resvTime
    """)
    List<BossResvDetailView> findByResvState(@Param("shopCode") Integer shopCode);

    /*
        특정 날짜와 시간에 예약 시간으로 예약 조회하면서 비관럭 락(PESSIMISTIC_WRITE) 설정
        이 메소드를 호출한 트랜잭션이 완료될 때까지 다른 트랜잭션은 이 조건에 해당하는 데이터에 접근할 수 없다.
    */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BossReservation> findByShopInfo_ShopCodeAndResvDateAndResvTime(Integer shopCode, Date resvDate, Time resvTime);
}
