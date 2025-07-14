package com.header.header.domain.reservation.repository;

import com.header.header.common.exception.NotFoundException;
import com.header.header.domain.reservation.dto.BossResvInputDTO;
import com.header.header.domain.reservation.dto.BossResvProjectionDTO;
import com.header.header.domain.reservation.entity.BossReservation;
import com.header.header.domain.reservation.enums.ReservationState;
import com.header.header.domain.reservation.service.BossReservationService;
import com.header.header.domain.sales.dto.SalesDTO;
import com.header.header.domain.sales.dto.SalesDetailDTO;
import com.header.header.domain.sales.enums.PaymentStatus;
import com.header.header.domain.sales.repository.SalesRepository;
import com.header.header.domain.sales.service.SalesService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class BossReservationRepositoryTests {

    @Autowired
    private BossReservationService bossReservationService;
    @Autowired
    SalesService salesService;

    // 테스트용 샵코드
    private static final Integer SHOP_CODE = 1;
    @Autowired
    private BossReservationRepository bossReservationRepository;

    @Test
    @Order(1)
    @DisplayName("가게의 전체 예약 내역 조회하기")
    void testReservationList(){
        List<BossResvProjectionDTO> reservationDTOList = bossReservationService.findReservationList(SHOP_CODE);

        assertNotNull(reservationDTOList);

        System.out.println("DTO");
        reservationDTOList.forEach(
                row -> System.out.println(row)
        );
    }

    @Test
    @Order(2)
    @DisplayName("날짜 별 가게 예약 내역 조회하기")
    void testReservationListByDate(){
        // given
        java.util.Date utilDate = new java.util.Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("2025-07-26");
        String formattedDate = dateFormat.format(utilDate);
        Date selectedDate = Date.valueOf(formattedDate);

        // when
        List<BossResvProjectionDTO> reservationDTOList = bossReservationService.findReservationListByDate(SHOP_CODE, selectedDate);

        assertNotNull(reservationDTOList);

        System.out.println("DTO");
        reservationDTOList.forEach(
                row -> System.out.println(row)
        );
    }

    @Test
    @Order(3)
    @DisplayName("고객 이름 별 가게 예약 조회하기")
    void testReservationListByUserName(){
        // given
        String userName = "홍길동";

        // when
        List<BossResvProjectionDTO> reservationDTOList = bossReservationService.findReservationListByName(SHOP_CODE, userName);

        assertNotNull(reservationDTOList);

        System.out.println("DTO");
        reservationDTOList.forEach(
                row -> System.out.println(row)
        );
    }

    @Test
    @Order(4)
    @DisplayName("예약 번호로 예약 상세 내역 조회하기")
    void testDetailReservationByResvCode() {
        // given
        int resvCode = 6;

        // when
        BossResvProjectionDTO reservation = bossReservationService.findReservationByResvCode(resvCode);

        // then
        assertNotNull(reservation);
        assertEquals(reservation.getUserName(), "손흥민");

        System.out.println(reservation);
    }

    @Test
    @Order(5)
    @DisplayName("메뉴 이름으로 예약 내역 조회")
    void testReservationListByMenuName(){
        // given
        String menuName = "파마";

        // when
        List<BossResvProjectionDTO> reservationList = bossReservationService.findReservationListByMenuName(SHOP_CODE, menuName);

        //then
        assertNotNull(reservationList);

        reservationList.forEach(
                row -> System.out.println(row)
        );
    }

    private static Stream<Arguments> newReservations(){
        return Stream.of(Arguments.of("유은비", "010-2025-0713", "여성 컷", Date.valueOf("2025-07-14"), Time.valueOf("14:00:00"), "둥근 얼굴에 어울리는 단발을 원해요.")
//        return Stream.of(Arguments.of("권은지", "010-1002-1002", "브라질리언 왁싱", Date.valueOf("2025-07-15"), Time.valueOf("16:00:00"), "")
                );
    }

    @ParameterizedTest
    @Order(6)
    @MethodSource("newReservations")
    @DisplayName("새로운 예약 등록하기")
    void testRegistNewReservation(String userName, String userPhone, String menuName, Date resvDate, Time resvTime, String userComment){

        // when
        BossResvInputDTO newReservation = new BossResvInputDTO(userName, userPhone, SHOP_CODE, menuName, resvDate, resvTime, userComment);
        bossReservationService.registNewReservation(newReservation);

        // then
        List<BossResvProjectionDTO> saved = bossReservationService.findReservationByUserNameAndUserPhone(SHOP_CODE, userName, userPhone);
        assertNotNull(saved);
    }

    private static Stream<Arguments> modifiedReservation(){
        return Stream.of(Arguments.of("포인트 염색", Date.valueOf("2025-07-13"), Time.valueOf("14:00:00"), "")
        );
    }

    @ParameterizedTest
    @Order(7)
    @MethodSource("modifiedReservation")
    @DisplayName("예약 내용 수정하기")
    void testModifyReservation(String menuName, Date resvDate, Time resvTime, String userComment){
        // given
        Integer resvCode = 70;
        BossResvInputDTO inputDTO = new BossResvInputDTO(menuName, resvDate, resvTime, userComment, SHOP_CODE);

        // when
        bossReservationService.updateReservation(inputDTO, resvCode);

        // then
        BossResvProjectionDTO saved = bossReservationService.findReservationByResvCode(resvCode);
//        assertEquals(saved.getMenuName(), );
        assertEquals(saved.getResvDate(), Date.valueOf("2025-07-13"));
        System.out.println(saved);
    }

    @Test
    @Order(8)
    @DisplayName("예약 취소하기 - 논리적 삭제")
    void testCancleResercation(){
        // given
        int resvCode = 87;

        // when
        bossReservationService.cancelReservation(resvCode);

        // then
        BossReservation canceled = bossReservationRepository.findById(resvCode).orElseThrow(IllegalArgumentException::new);
        assertEquals(canceled.getResvState(), ReservationState.CANCEL);
    }

    @Test
    @DisplayName("예약 삭제하기 - 물리적 삭제")
    void testDeleteReservation(){
        // given
        Integer resvCode = 88;

        // when
        bossReservationService.deleteReservation(resvCode);

        // then
        assertThrowsExactly(
                NotFoundException.class,
                () -> bossReservationService.findReservationByResvCode(resvCode)
        );
    }

    private static Stream<Arguments> salesInfo(){
        return Stream.of(Arguments.of(5, 120000, "신용카드", LocalDateTime.now(), PaymentStatus.COMPLETED, 0)
        );
    }

    @ParameterizedTest
    @Order(9)
    @MethodSource("salesInfo")
    @DisplayName("시술 완료 시 매출 테이블에 데이터 넣기")
    void testAfterProcedure(Integer resvCode, Integer payAmount, String payMethod, LocalDateTime payDateTime, PaymentStatus payStatus, Integer cancelAmount){

        // when
        SalesDTO salesDTO = new SalesDTO();
        salesDTO.setResvCode(resvCode);
        salesDTO.setPayAmount(payAmount);
        salesDTO.setPayMethod(payMethod);
        salesDTO.setPayDatetime(payDateTime);
        salesDTO.setPayStatus(payStatus);
        salesDTO.setCancelAmount(cancelAmount);

        // when
        bossReservationService.afterProcedure(salesDTO);

        // then
//        BossResvProjectionDTO reservation = bossReservationService.findReservationByResvCode(resvCode);
        List<SalesDetailDTO> salesList =  salesService.getSalesDetailsByShop(SHOP_CODE);

        assertEquals(salesList.size(), 3);
    }

    @Test
    @Order(10)
    @DisplayName("노쇼 리스트 조회")
    void testNoShowList(){
        // given
        LocalDate now = LocalDate.now();
        Date today = Date.valueOf(String.valueOf(now));
        ReservationState resvState = ReservationState.APPROVE;

        // when
        List<BossResvProjectionDTO> noShowList = bossReservationService.findNoShowList(today, resvState, SHOP_CODE);

        // then
        assertNotNull(noShowList);
        noShowList.forEach(
                row -> System.out.println(row)
        );
    }

    @Test
    @Order(11)
    @DisplayName("노쇼 처리")
    void testNoShow(){
        // given
        Integer resvCode = 8;

        // when
        bossReservationService.noShowHandler(resvCode);

        // then
        BossReservation noShow = bossReservationRepository.findById(resvCode).orElseThrow(IllegalArgumentException::new);
        assertEquals(noShow.getResvState(), ReservationState.CANCEL);
        assertEquals(noShow.getUserComment(), "노쇼");
        System.out.println("noShow = " + noShow);
    }
}
