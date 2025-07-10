package com.header.header.domain.sales.service;

import static org.junit.jupiter.api.Assertions.*;

import com.header.header.domain.sales.dto.SalesDTO;
import com.header.header.domain.sales.dto.SalesDetailDTO;
import com.header.header.domain.sales.enums.PaymentStatus;
import com.header.header.common.exception.NotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
public class SalesServiceTests {

    @Autowired
    private SalesService salesService;

    // 테스트용 데이터 상수
    private static final Integer TEST_RESV_CODE_1 = 3;
    private static final Integer TEST_RESV_CODE_2 = 4;
    private static final Integer TEST_RESV_CODE_3 = 5;
    private static final Integer TEST_RESV_CODE_4 = 6;
    private static final Integer TEST_RESV_CODE_5 = 7;
    private static final Integer TEST_SHOP_CODE = 1;
    private static final Integer TEST_PAY_AMOUNT = 25000;
    private static final String TEST_PAY_METHOD = "카드";
    private static final String TEST_CANCEL_REASON = "고객 요청";

    // 생성된 매출 코드를 저장할 변수 (테스트 간 공유)
    private static Integer createdSalesCode;

    @Test
    @Order(1)
    @DisplayName("결제 생성 테스트 - 정상 케이스")
    void testCreatePayment() {
        // given
        SalesDTO salesDTO = new SalesDTO();
        salesDTO.setResvCode(TEST_RESV_CODE_1);
        salesDTO.setPayAmount(TEST_PAY_AMOUNT);
        salesDTO.setPayMethod(TEST_PAY_METHOD);

        // when
        SalesDTO createdSales = salesService.createPayment(salesDTO);

        // then
        assertNotNull(createdSales);
        assertNotNull(createdSales.getSalesCode());
        assertEquals(TEST_RESV_CODE_1, createdSales.getResvCode());
        assertEquals(TEST_PAY_AMOUNT, createdSales.getPayAmount());
        assertEquals(TEST_PAY_METHOD, createdSales.getPayMethod());
        assertEquals(PaymentStatus.COMPLETED, createdSales.getPayStatus());
        assertEquals(0, createdSales.getCancelAmount());
        assertEquals(TEST_PAY_AMOUNT, createdSales.getFinalAmount());
        assertNotNull(createdSales.getPayDatetime());

        // 생성된 매출 코드 저장
        createdSalesCode = createdSales.getSalesCode();

        System.out.println("생성된 매출: " + createdSales);
    }

    @Test
    @Order(2)
    @DisplayName("중복 결제 생성 시 예외 발생 테스트")
    void testCreatePaymentDuplicate() {
        // given - 먼저 결제 생성
        SalesDTO firstSalesDTO = new SalesDTO();
        firstSalesDTO.setResvCode(TEST_RESV_CODE_2);
        firstSalesDTO.setPayAmount(TEST_PAY_AMOUNT);
        firstSalesDTO.setPayMethod(TEST_PAY_METHOD);
        salesService.createPayment(firstSalesDTO);

        // 같은 예약에 대한 중복 결제 시도
        SalesDTO duplicateSalesDTO = new SalesDTO();
        duplicateSalesDTO.setResvCode(TEST_RESV_CODE_2); // 동일한 예약 코드
        duplicateSalesDTO.setPayAmount(TEST_PAY_AMOUNT);
        duplicateSalesDTO.setPayMethod(TEST_PAY_METHOD);

        // when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            salesService.createPayment(duplicateSalesDTO);
        });

        assertTrue(exception.getMessage().contains("이미 해당 예약에 대한 결제가 존재합니다"));
        System.out.println("중복 결제 예외: " + exception.getMessage());
    }

    @Test
    @Order(3)
    @DisplayName("매출 상세 정보 조회 테스트")
    void testGetSalesDetail() {
        // given
        assertNotNull(1, "먼저 매출이 생성되어야 합니다.");

        // when
        SalesDetailDTO salesDetail = salesService.getSalesDetail(1);

        // then
        assertNotNull(salesDetail);
        assertEquals(1, salesDetail.getSalesCode());
        assertNotNull(salesDetail.getUserName());
        assertNotNull(salesDetail.getMenuName());

        System.out.println("조회된 매출 상세: " + salesDetail);
    }

    @Test
    @Order(4)
    @DisplayName("예약 코드로 매출 상세 정보 조회 테스트")
    void testGetSalesDetailByReservation() {
        // when
        SalesDetailDTO salesDetail = salesService.getSalesDetailByReservation(TEST_RESV_CODE_1);

        // then
        assertNotNull(salesDetail);
        assertEquals(TEST_RESV_CODE_1, salesDetail.getResvCode());
        assertEquals(TEST_PAY_AMOUNT, salesDetail.getPayAmount());

        System.out.println("예약으로 조회된 매출 상세: " + salesDetail);
    }

    @Test
    @Order(5)
    @DisplayName("특정 샵의 매출 상세 목록 조회 테스트")
    void testGetSalesDetailsByShop() {
        // when
        List<SalesDetailDTO> salesDetailList = salesService.getSalesDetailsByShop(TEST_SHOP_CODE);

        // then
        assertNotNull(salesDetailList);
        assertTrue(salesDetailList.size() > 0);

        // 생성한 매출이 포함되어 있는지 확인
        boolean containsCreatedSales = salesDetailList.stream()
            .anyMatch(sales -> sales.getSalesCode().equals(createdSalesCode));
        assertTrue(containsCreatedSales);

        System.out.println("샵의 매출 개수: " + salesDetailList.size());
        salesDetailList.forEach(sales -> System.out.println("매출 정보: " + sales));
    }

    @Test
    @Order(6)
    @DisplayName("특정 샵의 완료된 매출만 조회 테스트")
    void testGetCompletedSalesDetailsByShop() {
        // when
        List<SalesDetailDTO> completedSales = salesService.getCompletedSalesDetailsByShop(TEST_SHOP_CODE);

        // then
        assertNotNull(completedSales);
        assertTrue(completedSales.size() > 0);

        // 모든 매출이 완료 상태인지 확인 (PaymentStatus enum과 비교)
        completedSales.forEach(sales ->
            assertEquals(PaymentStatus.COMPLETED, sales.getPayStatus()));

        System.out.println("완료된 매출 개수: " + completedSales.size());
    }

    @Test
    @Order(7)
    @DisplayName("결제 정보 수정 테스트")
    void testUpdatePayment() {
        // given
        assertNotNull(createdSalesCode, "먼저 매출이 생성되어야 합니다.");

        SalesDTO updateDTO = new SalesDTO();
        updateDTO.setPayMethod("현금");
        updateDTO.setCancelReason("변경 요청");

        // when
        SalesDTO updatedSales = salesService.updatePayment(createdSalesCode, updateDTO);

        // then
        assertNotNull(updatedSales);
        assertEquals("현금", updatedSales.getPayMethod());
        assertEquals("변경 요청", updatedSales.getCancelReason());

        System.out.println("수정된 매출: " + updatedSales);
    }

    @Test
    @Order(8)
    @DisplayName("부분 취소 테스트")
    void testPartialCancelPayment() {
        // given
        assertNotNull(createdSalesCode, "먼저 매출이 생성되어야 합니다.");
        Integer cancelAmount = 5000; // 부분 취소 금액

        // when
        SalesDTO cancelledSales = salesService.cancelPayment(createdSalesCode, cancelAmount, TEST_CANCEL_REASON);

        // then
        assertNotNull(cancelledSales);
        assertEquals(PaymentStatus.PARTIAL_CANCELLED, cancelledSales.getPayStatus());
        assertEquals(cancelAmount, cancelledSales.getCancelAmount());
        assertEquals(TEST_PAY_AMOUNT - cancelAmount, cancelledSales.getFinalAmount());
        assertEquals(TEST_CANCEL_REASON, cancelledSales.getCancelReason());
        assertNotNull(cancelledSales.getCancelDatetime());

        System.out.println("부분 취소된 매출: " + cancelledSales);
    }

    @Test
    @Order(9)
    @DisplayName("전체 취소 테스트")
    void testFullCancelPayment() {
        // given
        // 새로운 결제 생성 (전체 취소 테스트용)
        SalesDTO newSalesDTO = new SalesDTO();
        newSalesDTO.setResvCode(TEST_RESV_CODE_3); // 다른 예약 코드
        newSalesDTO.setPayAmount(15000);
        newSalesDTO.setPayMethod("카드");

        SalesDTO newSales = salesService.createPayment(newSalesDTO);
        Integer newSalesCode = newSales.getSalesCode();

        // when
        SalesDTO cancelledSales = salesService.cancelPayment(newSalesCode, 15000, "전체 취소");

        // then
        assertNotNull(cancelledSales);
        assertEquals(PaymentStatus.CANCELLED, cancelledSales.getPayStatus());
        assertEquals(15000, cancelledSales.getCancelAmount());
        assertEquals(0, cancelledSales.getFinalAmount());

        System.out.println("전체 취소된 매출: " + cancelledSales);
    }

    @Test
    @Order(10)
    @DisplayName("취소된 매출 목록 조회 테스트")
    void testGetCancelledSalesDetailsByShop() {
        // when
        List<SalesDetailDTO> cancelledSales = salesService.getCancelledSalesDetailsByShop(TEST_SHOP_CODE);

        // then
        assertNotNull(cancelledSales);
        assertTrue(cancelledSales.size() > 0);

        // PaymentStatus enum의 isCancelled() 메서드를 사용하여 검증
        cancelledSales.forEach(sales ->
            assertTrue(sales.getPayStatus().isCancelled()));

        System.out.println("취소된 매출 개수: " + cancelledSales.size());
    }

    @Test
    @Order(11)
    @DisplayName("기간별 매출 조회 테스트")
    void testGetSalesDetailsByShopAndDateRange() {
        // given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // when
        List<SalesDetailDTO> salesInRange = salesService.getSalesDetailsByShopAndDateRange(
            TEST_SHOP_CODE, startDate, endDate);

        // then
        assertNotNull(salesInRange);
        assertTrue(salesInRange.size() > 0);

        // 생성한 매출이 포함되어 있는지 확인
        boolean containsCreatedSales = salesInRange.stream()
            .anyMatch(sales -> sales.getSalesCode().equals(createdSalesCode));
        assertTrue(containsCreatedSales);

        System.out.println("기간별 매출 개수: " + salesInRange.size());
    }

    @Test
    @Order(12)
    @DisplayName("매출 통계 조회 테스트")
    void testCalculateTotalSales() {
        // given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // when
        Long totalSales = salesService.calculateTotalSales(TEST_SHOP_CODE, startDate, endDate);

        // then
        assertNotNull(totalSales);
        assertTrue(totalSales >= 0);

        System.out.println("총 매출 금액: " + totalSales);
    }

    @Test
    @Order(13)
    @DisplayName("취소 금액 통계 조회 테스트")
    void testCalculateTotalCancelAmount() {
        // given
        LocalDateTime startDate = LocalDateTime.now().minusDays(1);
        LocalDateTime endDate = LocalDateTime.now().plusDays(1);

        // when
        Long totalCancelAmount = salesService.calculateTotalCancelAmount(TEST_SHOP_CODE, startDate, endDate);

        // then
        assertNotNull(totalCancelAmount);
        assertTrue(totalCancelAmount >= 0);

        System.out.println("총 취소 금액: " + totalCancelAmount);
    }

    @Test
    @Order(14)
    @DisplayName("결제 방법별 통계 조회 테스트")
    void testGetSalesStatsByPayMethod() {
        // when
        List<Object[]> payMethodStats = salesService.getSalesStatsByPayMethod(TEST_SHOP_CODE);

        // then
        assertNotNull(payMethodStats);
        assertTrue(payMethodStats.size() > 0);

        payMethodStats.forEach(stat -> {
            String payMethod = (String) stat[0];
            Long totalAmount = (Long) stat[1];
            Long count = (Long) stat[2];

            assertNotNull(payMethod);
            assertNotNull(totalAmount);
            assertNotNull(count);

            System.out.println("결제방법: " + payMethod + ", 총금액: " + totalAmount + ", 건수: " + count);
        });
    }

    @Test
    @Order(15)
    @DisplayName("월별 매출 통계 조회 테스트")
    void testGetMonthlySalesStats() {
        // when
        List<Object[]> monthlyStats = salesService.getMonthlySalesStats(TEST_SHOP_CODE);

        // then
        assertNotNull(monthlyStats);
        assertTrue(monthlyStats.size() > 0);

        monthlyStats.forEach(stat -> {
            Integer year = (Integer) stat[0];
            Integer month = (Integer) stat[1];
            Long totalAmount = (Long) stat[2];
            Long count = (Long) stat[3];

            assertNotNull(year);
            assertNotNull(month);
            assertNotNull(totalAmount);
            assertNotNull(count);

            System.out.println(year + "년 " + month + "월 - 총금액: " + totalAmount + ", 건수: " + count);
        });
    }

    @Test
    @Order(16)
    @DisplayName("활성 결제 조회 테스트")
    void testGetActivePaymentsByShop() {
        // when
        List<SalesDTO> activePayments = salesService.getActivePaymentsByShop(TEST_SHOP_CODE);

        // then
        assertNotNull(activePayments);
        assertTrue(activePayments.size() > 0);

        // 모든 결제가 삭제되지 않은 상태인지 확인
        activePayments.forEach(payment ->
            assertNotEquals(PaymentStatus.DELETED, payment.getPayStatus()));

        System.out.println("활성 결제 개수: " + activePayments.size());
    }

    @Test
    @Order(17)
    @DisplayName("고액 결제 조회 테스트")
    void testGetHighAmountPaymentsByShop() {
        // given
        Integer minimumAmount = 20000;

        // when
        List<SalesDTO> highAmountPayments = salesService.getHighAmountPaymentsByShop(TEST_SHOP_CODE, minimumAmount);

        // then
        assertNotNull(highAmountPayments);

        // 모든 결제가 최소 금액 이상인지 확인
        highAmountPayments.forEach(payment ->
            assertTrue(payment.getPayAmount() >= minimumAmount));

        System.out.println("고액 결제 개수: " + highAmountPayments.size());
    }

    @Test
    @Order(18)
    @DisplayName("매출 논리적 삭제 테스트")
    void testDeleteSales() {
        // given
        assertNotNull(createdSalesCode, "먼저 매출이 생성되어야 합니다.");

        // when
        salesService.deleteSales(createdSalesCode);

        // then - 매출은 존재하지만 삭제 상태여야 함
        SalesDetailDTO deletedSales = salesService.getSalesDetail(createdSalesCode);
        assertNotNull(deletedSales);
        assertEquals(PaymentStatus.DELETED, deletedSales.getPayStatus());

        System.out.println("논리적 삭제된 매출: " + deletedSales);
    }

    @Test
    @Order(19)
    @DisplayName("존재하지 않는 매출 조회 시 예외 발생 테스트")
    void testGetSalesNotFoundException() {
        // given
        Integer nonExistentSalesCode = 99999;

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            salesService.getSalesDetail(nonExistentSalesCode);
        });

        assertTrue(exception.getMessage().contains("매출"));
        System.out.println("예외 메시지: " + exception.getMessage());
    }

    @Test
    @Order(20)
    @DisplayName("존재하지 않는 예약의 매출 조회 시 예외 발생 테스트")
    void testGetSalesDetailByReservationNotFoundException() {
        // given
        Integer nonExistentResvCode = 99999;

        // when & then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            salesService.getSalesDetailByReservation(nonExistentResvCode);
        });

        assertTrue(exception.getMessage().contains("매출"));
        System.out.println("예외 메시지: " + exception.getMessage());
    }

    @Test
    @Order(21)
    @DisplayName("잘못된 취소 금액으로 취소 시 예외 발생 테스트")
    void testCancelPaymentInvalidAmount() {
        // given
        // 새로운 결제 생성
        SalesDTO newSalesDTO = new SalesDTO();
        newSalesDTO.setResvCode(TEST_RESV_CODE_4);
        newSalesDTO.setPayAmount(10000);
        newSalesDTO.setPayMethod("카드");

        SalesDTO newSales = salesService.createPayment(newSalesDTO);

        // when & then - 결제 금액보다 큰 취소 금액
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            salesService.cancelPayment(newSales.getSalesCode(), 15000, "잘못된 취소");
        });

        assertTrue(exception.getMessage().contains("취소 금액이 남은 금액을 초과합니다"));
        System.out.println("잘못된 취소 금액 예외: " + exception.getMessage());
    }

    @Test
    @Order(22)
    @DisplayName("0 이하의 취소 금액으로 취소 시 예외 발생 테스트")
    void testCancelPaymentZeroAmount() {
        // given
        // 새로운 결제 생성
        SalesDTO newSalesDTO = new SalesDTO();
        newSalesDTO.setResvCode(TEST_RESV_CODE_5);
        newSalesDTO.setPayAmount(10000);
        newSalesDTO.setPayMethod("카드");

        SalesDTO newSales = salesService.createPayment(newSalesDTO);

        // when & then - 0 이하의 취소 금액
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            salesService.cancelPayment(newSales.getSalesCode(), 0, "잘못된 취소");
        });

        assertTrue(exception.getMessage().contains("취소 금액은 0보다 커야 합니다"));
        System.out.println("0 이하 취소 금액 예외: " + exception.getMessage());
    }

    @Test
    @Order(23)
    @DisplayName("이미 취소된 결제를 다시 취소할 때 예외 발생 테스트")
    void testCancelAlreadyCancelledPayment() {

        // when & then - 이미 취소된 결제를 다시 취소
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            salesService.cancelPayment(2, 20000, "재취소 시도");
        });

        assertTrue(exception.getMessage().contains("현재 상태에서는 취소할 수 없습니다"));
        System.out.println("재취소 예외: " + exception.getMessage());
    }
}