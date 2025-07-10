package com.header.header.domain.reservation.repository;

import com.header.header.common.exception.NotFoundException;
import com.header.header.domain.reservation.dto.BasicReservationDTO;
import com.header.header.domain.reservation.dto.BossReservationDTO;
import com.header.header.domain.reservation.dto.BossResvInputDTO;
import com.header.header.domain.reservation.entity.BossReservation;
import com.header.header.domain.reservation.entity.Reservation;
import com.header.header.domain.reservation.service.BossReservationService;
import com.header.header.domain.sales.dto.SalesDTO;
import com.header.header.domain.sales.enums.PaymentStatus;
import com.header.header.domain.sales.repository.SalesRepository;
import com.header.header.domain.sales.service.SalesService;
import org.hibernate.Internal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BossReservationRepositoryTests {

    @Autowired
    private BossReservationService bossReservationService;
    @Autowired
    SalesRepository salesRepository;

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
        int resvCode = 40;

        // when
        BossReservationDTO reservation = bossReservationService.findReservationByResvCode(resvCode);

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

    private static Stream<Arguments> newReservations(){
        return Stream.of(Arguments.of("이하나", "010-8899-7766", "젤 네일 프렌치", Date.valueOf("2025-07-11"), Time.valueOf("14:00:00"), "짧은 손톱에 어울리게 해주세요.")
//        return Stream.of(Arguments.of("권은지", "010-1002-1002", "브라질리언 왁싱", Date.valueOf("2025-07-15"), Time.valueOf("16:00:00"), "")
                );
    }

    @ParameterizedTest
    @MethodSource("newReservations")
    @DisplayName("새로운 예약 등록하기")
    void testRegistNewReservation(String userName, String userPhone, String menuName, Date resvDate, Time resvTime, String userComment){

        // when
        BossResvInputDTO newReservation = new BossResvInputDTO(userName, userPhone, SHOP_CODE, menuName, resvDate, resvTime, userComment);
        bossReservationService.registNewReservation(newReservation);

        // then
        List<BossReservationDTO> saved = bossReservationService.findReservationByUserNameAndUserPhone(SHOP_CODE, userName, userPhone);
        assertNotNull(saved);
    }

    private static Stream<Arguments> modifiedReservation(){
        return Stream.of(Arguments.of("젤 네일 풀컬러", Date.valueOf("2025-07-13"), Time.valueOf("14:00:00"), "짧은 손톱에 어울리게 해주세요.")
        );
    }

    @ParameterizedTest
    @MethodSource("modifiedReservation")
    @DisplayName("예약 내용 수정하기")
    void testModifyReservation(String menuName, Date resvDate, Time resvTime, String userComment){
        // given
        Integer resvCode = 30;
        BossResvInputDTO inputDTO = new BossResvInputDTO(menuName, resvDate, resvTime, userComment);

        // when
        bossReservationService.updateReservation(inputDTO, resvCode);

        // then
        BossReservationDTO saved = bossReservationService.findReservationByResvCode(resvCode);
        assertEquals(saved.getMenuInfo().getMenuCode(), 7);
        assertEquals(saved.getResvDate(), Date.valueOf("2025-07-13"));
        System.out.println(saved);
    }

    @Test
    @DisplayName("예약 취소하기 - 논리적 삭제")
    void testCancleResercation(){
        // given
        int resvCode = 30;

        // when
        bossReservationService.cancelReservation(resvCode);

        // then
        BossReservationDTO cancled = bossReservationService.findReservationByResvCode(resvCode);
        assertEquals(cancled.getResvState(), "예약취소");
    }

    @Test
    @DisplayName("예약 삭제하기 - 물리적 삭제")
    void testDeleteReservation(){
        // given
        Integer resvCode = 32;

        // when
        bossReservationService.deleteReservation(resvCode);

        // then
        assertThrowsExactly(
                NotFoundException.class,
                () -> bossReservationService.findReservationByResvCode(resvCode)
        );
    }

    private static Stream<Arguments> salesInfo(){
        return Stream.of(Arguments.of(30, 80000, "신용카드", LocalDateTime.of(2025, 7, 7, 11, 0, 0), PaymentStatus.COMPLETED, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("salesInfo")
    @DisplayName("시술 완료 시 매출 테이블에 데이터 넣기")
    void testAfterProcedure(Integer resvCode, Integer payAmount, String payMetod, LocalDateTime payDateTime, PaymentStatus payStatus, Integer cancelAmount){

        // when
        SalesDTO salesDTO = new SalesDTO();
        salesDTO.setResvCode(resvCode);
        salesDTO.setPayAmount(payAmount);
        salesDTO.setPayMethod(payMetod);
        salesDTO.setPayDatetime(payDateTime);
        salesDTO.setPayStatus(payStatus);
        salesDTO.setCancelAmount(cancelAmount);

        bossReservationService.afterProcedure(salesDTO);

        assertNotNull(salesRepository.existsByResvCode(resvCode));
    }
}
