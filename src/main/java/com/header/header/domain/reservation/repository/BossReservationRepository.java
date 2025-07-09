package com.header.header.domain.reservation.repository;

import com.header.header.domain.reservation.entity.BossReservation;
import com.header.header.domain.reservation.entity.Reservation;
import com.header.header.domain.user.enitity.User;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Date;
import java.util.List;

public interface BossReservationRepository extends JpaRepository<BossReservation, Integer> {

    String baiscQuery = "SELECT r.resv_code" +
            "                 , u.user_code" +
            "                 , u.user_name" +
            "                 , u.user_phone" +
            "                 , u.birthday" +
            "                 , r.shop_code" +
            "                 , m.menu_code" +
            "                 , m.menu_name" +
            "                 , m.menu_price" +
            "                 , m.est_time" +
            "                 , m.is_active" +
            "                 , m.menu_color" +
            "                 , r.resv_date" +
            "                 , r.resv_time" +
            "                 , r.user_comment" +
            "                 , r.resv_state" +
            "              FROM tbl_reservation r " +
            "             INNER JOIN tbl_user u ON(r.user_code = u.user_code)" +
            "             INNER JOIN (SELECT mn.menu_code" +
            "                              , mn.menu_name" +
            "                              , mn.menu_price" +
            "                              , mn.est_time" +
            "                              , mn.is_active" +
            "                              , mc.menu_color" +
            "                           FROM tbl_menu mn" +
            "                           JOIN tbl_menu_category mc ON (mn.category_code = mc.category_code and mn.shop_code = mc.shop_code)) m ON(r.menu_code = m.menu_code)" +
            "             WHERE r.shop_code = :shopCode";

    /* 모든 예약 내역 조회하기 */
    @Query( nativeQuery = true,
            value = baiscQuery
    )
    List<BossReservation> findByShopCode(@Param("shopCode")Integer shopCode);

    /* 선택된 날짜의 예약 조회 */
    @Query( nativeQuery = true,
            value = baiscQuery + " AND r.resv_date = :selectedDate"
    )
    List<BossReservation> findByShopCodeAndResvDate(@Param("shopCode") Integer shopCode, @Param("selectedDate") Date selectedDate);

    /* 고객 이름으로 예약 조회 */
    @Query( nativeQuery = true,
            value = baiscQuery + " AND u.user_name LIKE CONCAT('%', :userName, '%')"
    )
    List<BossReservation> findByShopCodeAndUserName(@Param("shopCode") Integer shopCode, @Param("userName") String userName);

    /* 예약 번호르 예약 상세조회 */
    @Query( nativeQuery = true,
            value =  baiscQuery + " AND r.resv_code = :resvCode"
    )
    BossReservation findByResvCode(@Param("shopCode") Integer shopCode, @Param("resvCode") Integer resvCode);

    @Query( nativeQuery = true,
            value = baiscQuery + " AND m.menu_name LIKE CONCAT('%', :menuName, '%')"
    )
    List<BossReservation> findByShopCodeAndMenuName(@Param("shopCode") Integer shopCode, @Param("menuName") String menuName);


}
