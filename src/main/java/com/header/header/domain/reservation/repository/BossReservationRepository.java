package com.header.header.domain.reservation.repository;

import com.header.header.domain.reservation.entity.BossReservation;
import com.header.header.domain.reservation.enums.ReservationState;
import com.header.header.domain.reservation.projection.BossResvDetailView;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Date;
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
    """)
    List<BossResvDetailView> findByShopCode(@Param("shopCode")Integer shopCode);

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
    """)
    List<BossResvDetailView> findByResvDateAndResvState(@Param("today") Date today, @Param("resvState") ReservationState resvState, @Param("shopCode") Integer shopCode);

//    @Query("""
//        SELECT r
//        FROM BossReservation r
//        JOIN r.userInfo u
//        JOIN r.shopInfo s
//        JOIN r.menuInfo m
//        JOIN m.menuCategory mc
//        WHERE r.resvCode = :resvCode
//    """)
//    BossReservation findReservationByResvCode(@Param("resvCode") Integer resvCode);

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

}
