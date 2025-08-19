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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
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
        // given
        String thisMonth = "2025-06";

        // when
        List<BossResvProjectionDTO> reservationDTOList = bossReservationService.findReservationList(SHOP_CODE, thisMonth);

        /** 오류 해결하고 pull 받기 */
        // then
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
                );
    }

    @ParameterizedTest
    @Order(6)
    @MethodSource("newReservations")
    @DisplayName("새로운 예약 등록하기")
    void testRegistNewReservation(String userName, String userPhone, String menuName, Date resvDate, Time resvTime, String userComment){

        // when
        BossResvInputDTO newReservation = new BossResvInputDTO(userName, userPhone, menuName, resvDate, resvTime, userComment);
        bossReservationService.registNewReservation(newReservation, SHOP_CODE);

        // then
        List<BossResvProjectionDTO> saved = bossReservationService.findReservationByUserNameAndUserPhone(SHOP_CODE, userName, userPhone);
        assertNotNull(saved);
    }

    private static Stream<Arguments> newReservations2(){
        return Stream.of(Arguments.of("김남기", "010-8978-0955", "전체 염색", Date.valueOf("2025-08-29"), Time.valueOf("15:00:00"), "단정한 색으로 염색 원해요.")
        );
    }

    @ParameterizedTest
    @MethodSource("newReservations2")
    @DisplayName("예약 동시성 문제 테스트 -> 동시에 100개의 예약 요청이 들어올 경우 단 1개만 성공해야 한다.")
    void concurrentReservations(String userName, String userPhone, String menuName, Date resvDate, Time resvTime, String userComment) throws InterruptedException{
        // given
        int threadCount = 5;
        // ExecutorService : 스레드 풀을 생성하여 비동기 작업을 간단하게 처리하도록 돕는다.
        //                   즉 100개의 스레드를 가질 수 있는 스레드 풀을 생성한다.
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        // CountDownLatch : 특정 수의 스레드가 작업을 완료할 때까지 주 스레드가 대기하도록 돕는 동기화 도구
        //                  카운트가 100으로 초기화한다. 각 스레드는 작업이 끝나면 latch.countDown()을 호출하여 카운트를 1씩 줄인다.
        //                  메인 스레드는 작업이 끝나면 latch.await()에서 카운트가 0이 될 때까지 기다린다.
        CountDownLatch latch = new CountDownLatch(threadCount);

        // AtomicInteger : 멀티스레드 환경에서 동시성을 보장하면서 정수 값을 안전하게 증가/감소시킬 수 있다.
        //                 successCount와 failCount를 일반 int가 아닌 AtomicInteger로 선언
        //                 여러 스레드가 동시에 이 변수에 접근하여 값을 변경해도 race condition 없이 안전하게(thread-safe) 값이 관리된다.
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        for(int i = 0; i < threadCount; i++){
            final int threadNum = i;
            // executorService.submit : 스레드 풀에 작업을 제출.
            //                          각 작업은 bossReservationService.registNewReservation()을 호출하여 동일한 가게, 날짜, 시간에 예약을 시도
            executorService.submit(() -> {
                try{
                    BossResvInputDTO newReservation = new BossResvInputDTO(userName, userPhone, menuName, resvDate, resvTime, userComment);
                    bossReservationService.registNewReservation(newReservation, SHOP_CODE);
                    successCount.incrementAndGet(); // 성공 시 카운트
                } catch (Exception e){
                    // 예약 실패 시
                    failCount.incrementAndGet(); // 실패 시 카운트
                } finally {
                    latch.countDown(); // 작업 완료를 알림
                }
            });
        }

        latch.await(); // 모든 스레드가 작업을 마칠 때까지 대기
        executorService.shutdown();

        // then
        System.out.println("성공 카운트 : " + successCount.get());
        System.out.println("실패 카운트 : " + failCount.get());

        // 성공한 예약은 단 1개여야 함.
        assertThat(successCount.get()).isEqualTo(1);
        // 실패한 예약은 99개여야 한다.
        assertThat(failCount.get()).isEqualTo(threadCount - 1);
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
        BossResvInputDTO inputDTO = new BossResvInputDTO(menuName, resvDate, resvTime, userComment);

        // when
        bossReservationService.updateReservation(inputDTO, resvCode, SHOP_CODE);

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
