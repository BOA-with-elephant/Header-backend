package com.header.header.domain.reservation.repository;

import com.header.header.domain.reservation.dto.BossReservationDTO;
import com.header.header.domain.reservation.entity.Reservation;
import com.header.header.domain.reservation.service.BossReservationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BossReservationRepositoryTests {

    @Autowired
    private BossReservationService bossReservationService;
    @Autowired
    private BossReservationRepository bossReservationRepository;
    // 테스트용 샵코드
    private static final int SHOP_CODE = 1;

    @Test
    @DisplayName("가게의 전체 예약 내역 조회하기")
    void testReservationList(){
        List<BossReservationDTO> reservationDTOList = bossReservationService.findReservationList(SHOP_CODE);

        assertNotNull(reservationDTOList);

        System.out.println("DTO");
        reservationDTOList.forEach(
                row -> System.out.println(row)
        );
    }

    @Test
    @DisplayName("날짜 별 가게 예약 내역 조회하기")
    void testReservationListByDate(){
        // given
        java.util.Date utilDate = new java.util.Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("2025-07-06");
        String formattedDate = dateFormat.format(utilDate);
        Date selectedDate = Date.valueOf(formattedDate);

        // when
        List<BossReservationDTO> reservationDTOList = bossReservationService.findReservationListByDate(SHOP_CODE, selectedDate);

        assertNotNull(reservationDTOList);

        System.out.println("DTO");
        reservationDTOList.forEach(
                row -> System.out.println(row)
        );
    }

    @Test
    @DisplayName("고객 이름 별 가게 예약 조회하기")
    void testReservationListByUserName(){
        // given
        String userName = "유재석";

        // when
        List<BossReservationDTO> reservationDTOList = bossReservationService.findReservationListByName(SHOP_CODE, userName);

        assertNotNull(reservationDTOList);

        System.out.println("DTO");
        reservationDTOList.forEach(
                row -> System.out.println(row)
        );
    }

    @Test
    @DisplayName("예약 번호로 예약 상세 내역 조회하기")
    void testDetailReservationByResvCode() {
        // given
        int resvCode = 6;

        // when
        BossReservationDTO reservation = bossReservationService.findReservationByResvCode(SHOP_CODE, resvCode);

        // then
        assertNotNull(reservation);
        assertEquals(reservation.getUserInfo().getUserName(), "손흥민");

        System.out.println(reservation);
    }

    @Test
    @DisplayName("메뉴 이름으로 예약 내역 조회")
    void testReservationListByMenuName(){
        // given
        String menuName = "큐어";

        // when
        List<BossReservationDTO> reservationList = bossReservationService.findReservationListByMenuName(SHOP_CODE, menuName);

        //then
        assertNotNull(reservationList);

        reservationList.forEach(
                row -> System.out.println(row)
        );
    }

    @Test
    @DisplayName("예약 내역 삭제하기")
    void testCancleResercation(){
        // given
        int resvCode = 22;

        // when
        bossReservationService.cancleReservation(resvCode);

        // then
//        assertNull();
    }
}
