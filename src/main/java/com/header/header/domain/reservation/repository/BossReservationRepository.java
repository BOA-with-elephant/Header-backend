package com.header.header.domain.reservation.repository;

import com.header.header.domain.reservation.entity.BossReservation;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Date;
import java.util.List;

public interface BossReservationRepository extends JpaRepository<BossReservation, Integer> {

    @Query( "SELECT r FROM BossReservation r JOIN FETCH r.userInfo u JOIN FETCH r.shopInfo s JOIN FETCH r.menuInfo m JOIN FETCH m.menuCategory mc WHERE s.shopCode = :shopCode"
    )
    List<BossReservation> findByShopCode(@Param("shopCode")Integer shopCode);

    /* 선택된 날짜의 예약 조회 */
    @Query( "SELECT r FROM BossReservation r JOIN FETCH r.userInfo u JOIN FETCH r.shopInfo s JOIN FETCH r.menuInfo m JOIN FETCH m.menuCategory mc WHERE s.shopCode = :shopCode AND r.resvDate = :selectedDate"
    )
    List<BossReservation> findByShopCodeAndResvDate(@Param("shopCode") Integer shopCode, @Param("selectedDate") Date selectedDate);

    /* 고객 이름으로 예약 조회 */
    @Query( "SELECT r FROM BossReservation r JOIN FETCH r.userInfo u JOIN FETCH r.shopInfo s JOIN FETCH r.menuInfo m JOIN FETCH m.menuCategory mc WHERE s.shopCode = :shopCode AND u.userName LIKE CONCAT('%', :userName, '%')"
    )
    List<BossReservation> findByShopCodeAndUserName(@Param("shopCode") Integer shopCode, @Param("userName") String userName);

    /* 예약 번호로 예약 상세조회 */
    @Query( "SELECT r FROM BossReservation r JOIN FETCH r.userInfo u JOIN FETCH r.shopInfo s JOIN FETCH r.menuInfo m JOIN FETCH m.menuCategory mc WHERE r.resvCode = :resvCode"
    )
    BossReservation findByResvCode(@Param("resvCode") Integer resvCode);

    /* 메뉴 이름 별 예약 내역 조회 */
    @Query( "SELECT r FROM BossReservation r JOIN FETCH r.userInfo u JOIN FETCH r.shopInfo s JOIN FETCH r.menuInfo m JOIN FETCH m.menuCategory mc WHERE s.shopCode = :shopCode AND m.menuName LIKE CONCAT('%', :menuName, '%')"
    )
    List<BossReservation> findByShopCodeAndMenuName(@Param("shopCode") Integer shopCode, @Param("menuName") String menuName);

    /* 테스트용 */
    @Query( "SELECT r FROM BossReservation r JOIN FETCH r.userInfo u JOIN FETCH r.shopInfo s JOIN FETCH r.menuInfo m JOIN FETCH m.menuCategory mc WHERE s.shopCode = :shopCode AND u.userName = :userName AND u.userPhone = :userPhone"
    )
    List<BossReservation> findByUserNameAndUserPhone(@Param("shopCode") Integer shopCode, @Param("userName") String userName, @Param("userPhone") String userPhone);
}
