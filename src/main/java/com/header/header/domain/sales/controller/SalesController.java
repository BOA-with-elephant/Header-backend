package com.header.header.domain.sales.controller;

import com.header.header.domain.sales.dto.SalesDashboardDTO;
import com.header.header.domain.sales.dto.SalesDTO;
import com.header.header.domain.sales.dto.SalesDetailDTO;
import com.header.header.domain.sales.service.SalesService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"},
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class SalesController {

    private static final Logger log = LoggerFactory.getLogger(SalesController.class);

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    private <T> ResponseEntity<T> handleApiCall(String logMessage, Supplier<T> supplier) {
        try {
            log.debug(logMessage);
            T result = supplier.get();
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.error(logMessage + " 중 잘못된 요청", e);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error(logMessage + " 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // ========== 매출 등록/수정 관련 API 엔드포인트 ==========

    /**
     * 새로운 매출 등록
     * @param shopCode 매출을 등록할 샵 코드
     * @param salesDetailDTO 등록할 매출 정보 (SalesDetailDTO 사용)
     * @return 등록된 매출 정보
     */
    @PostMapping("/my-shops/{shopCode}/sales")
    public ResponseEntity<SalesDTO> createSales(@PathVariable Integer shopCode, @RequestBody SalesDetailDTO salesDetailDTO) {
        return handleApiCall("샵코드 " + shopCode + "의 새로운 매출 등록 요청", () -> {
            // SalesDetailDTO를 SalesDTO로 변환
            SalesDTO salesDTO = convertToSalesDTO(salesDetailDTO, shopCode);
            return salesService.createPayment(salesDTO);
        });
    }

    /**
     * 기존 매출 수정
     * @param shopCode 매출이 속한 샵 코드
     * @param salesCode 수정할 매출 코드
     * @param salesDetailDTO 수정할 매출 정보 (SalesDetailDTO 사용)
     * @return 수정된 매출 정보
     */
    @PutMapping("/my-shops/{shopCode}/sales/{salesCode}")
    public ResponseEntity<SalesDTO> updateSales(@PathVariable Integer shopCode,
        @PathVariable Integer salesCode,
        @RequestBody SalesDetailDTO salesDetailDTO) {
        return handleApiCall("샵코드 " + shopCode + ", 매출코드 " + salesCode + "의 매출 수정 요청", () -> {
            // SalesDetailDTO를 SalesDTO로 변환
            SalesDTO salesDTO = convertToSalesDTO(salesDetailDTO, shopCode);
            salesDTO.setSalesCode(salesCode);
            return salesService.updatePayment(salesCode, salesDTO);
        });
    }

    /**
     * 매출 취소 처리
     * @param shopCode 매출이 속한 샵 코드
     * @param salesCode 취소할 매출 코드
     * @param cancelRequest 취소 요청 정보 (cancelAmount, cancelReason 포함)
     * @return 취소 처리된 매출 정보
     */
    @PutMapping("/my-shops/{shopCode}/sales/{salesCode}/cancel")
    public ResponseEntity<SalesDTO> cancelSales(@PathVariable Integer shopCode,
        @PathVariable Integer salesCode,
        @RequestBody Map<String, Object> cancelRequest) {
        return handleApiCall("샵코드 " + shopCode + ", 매출코드 " + salesCode + "의 매출 취소 요청", () -> {
            Integer cancelAmount = Integer.valueOf(cancelRequest.get("cancelAmount").toString());
            String cancelReason = cancelRequest.get("cancelReason").toString();
            return salesService.cancelPayment(salesCode, cancelAmount, cancelReason);
        });
    }

    // ========== 기존 조회 관련 API 엔드포인트 ==========

    /**
     * 특정 샵의 전체 매출 조회 (삭제 제외)
     * @param shopCode 조회할 샵의 코드
     * @return 해당 샵의 모든 SalesDetail DTO 리스트
     */
    @GetMapping("/my-shops/{shopCode}/sales/active")
    public ResponseEntity<List<SalesDetailDTO>> getActiveSalesByShop(@PathVariable Integer shopCode) {
        return handleApiCall("샵코드 " + shopCode + "의 활성 매출 조회 요청",
            () -> salesService.getActiveSalesDetailsByShop(shopCode));
    }

    /**
     * 매출 삭제 (논리적 삭제)
     * @param shopCode 삭제할 매출이 속한 샵 코드
     * @param salesCode 삭제할 매출 코드
     * @return 삭제 완료 응답
     */
    @DeleteMapping("/my-shops/{shopCode}/sales/{salesCode}")
    public ResponseEntity<Void> deleteSales(@PathVariable Integer shopCode, @PathVariable Integer salesCode) {
        return handleApiCall("샵코드 " + shopCode + ", 매출코드 " + salesCode + "의 매출 삭제 요청", () -> {
            salesService.deleteSales(salesCode);
            return null;
        });
    }

    // ========== 매출 통계 관련 API 엔드포인트 ==========

    /**
     * 특정 샵의 기간별 총 매출 조회
     * @param shopCode 샵 코드
     * @param startDate 시작 날짜 (ISO 형식: yyyy-MM-ddTHH:mm:ss)
     * @param endDate 종료 날짜 (ISO 형식: yyyy-MM-ddTHH:mm:ss)
     * @return 총 매출 금액
     */
    @GetMapping("/my-shops/{shopCode}/sales/total-sales")
    public ResponseEntity<Long> getTotalSales(@PathVariable Integer shopCode,
        @RequestParam String startDate,
        @RequestParam String endDate) {
        LocalDateTime start = parseDateTime(startDate);
        LocalDateTime end = parseDateTime(endDate);
        return handleApiCall("샵코드 " + shopCode + "의 총 매출 조회",
            () -> salesService.calculateTotalSales(shopCode, start, end));
    }

    /**
     * 특정 샵의 기간별 총 취소 금액 조회
     * @param shopCode 샵 코드
     * @param startDate 시작 날짜 (ISO 형식: yyyy-MM-ddTHH:mm:ss)
     * @param endDate 종료 날짜 (ISO 형식: yyyy-MM-ddTHH:mm:ss)
     * @return 총 취소 금액
     */
    @GetMapping("/my-shops/{shopCode}/sales/total-cancel")
    public ResponseEntity<Long> getTotalCancelAmount(@PathVariable Integer shopCode,
        @RequestParam String startDate,
        @RequestParam String endDate) {
        LocalDateTime start = parseDateTime(startDate);
        LocalDateTime end = parseDateTime(endDate);
        return handleApiCall("샵코드 " + shopCode + "의 총 취소 금액 조회",
            () -> salesService.calculateTotalCancelAmount(shopCode, start, end));
    }

    /**
     * 특정 샵의 결제 방법별 매출 통계 조회
     * @param shopCode 샵 코드
     * @return 결제 방법별 통계 [결제방법, 총금액, 건수]
     */
    @GetMapping("/my-shops/{shopCode}/sales/payment-method-stats")
    public ResponseEntity<List<Object[]>> getPaymentMethodStats(@PathVariable Integer shopCode) {
        return handleApiCall("샵코드 " + shopCode + "의 결제 수단별 통계 조회",
            () -> salesService.getSalesStatsByPayMethod(shopCode));
    }

    /**
     * 특정 샵의 월별 매출 통계 조회
     * @param shopCode 샵 코드
     * @return 월별 통계 [년도, 월, 총금액, 건수]
     */
    @GetMapping("/my-shops/{shopCode}/sales/monthly-stats")
    public ResponseEntity<List<Object[]>> getMonthlySalesStats(@PathVariable Integer shopCode) {
        return handleApiCall("샵코드 " + shopCode + "의 월별 매출 통계 조회",
            () -> salesService.getMonthlySalesStats(shopCode));
    }

    /**
     * 특정 샵의 기간별 매출 상세 조회
     * @param shopCode 샵 코드
     * @param startDate 시작 날짜 (ISO 형식: yyyy-MM-ddTHH:mm:ss)
     * @param endDate 종료 날짜 (ISO 형식: yyyy-MM-ddTHH:mm:ss)
     * @return 기간별 매출 상세 목록
     */
    @GetMapping("/my-shops/{shopCode}/sales/period")
    public ResponseEntity<List<SalesDetailDTO>> getSalesInPeriod(@PathVariable Integer shopCode,
        @RequestParam String startDate,
        @RequestParam String endDate) {
        LocalDateTime start = parseDateTime(startDate);
        LocalDateTime end = parseDateTime(endDate);
        return handleApiCall("샵코드 " + shopCode + "의 기간별 매출 상세 조회",
            () -> salesService.getSalesDetailsByShopAndDateRange(shopCode, start, end));
    }

    /**
     * 특정 샵의 완료된 매출만 조회
     * @param shopCode 샵 코드
     * @return 완료된 매출 목록
     */
    @GetMapping("/my-shops/{shopCode}/sales/completed")
    public ResponseEntity<List<SalesDetailDTO>> getCompletedSalesByShop(@PathVariable Integer shopCode) {
        return handleApiCall("샵코드 " + shopCode + "의 완료된 매출 조회",
            () -> salesService.getCompletedSalesDetailsByShop(shopCode));
    }

    /**
     * 특정 샵의 취소된 매출만 조회
     * @param shopCode 샵 코드
     * @return 취소된 매출 목록 (전체취소 + 부분취소)
     */
    @GetMapping("/my-shops/{shopCode}/sales/cancelled")
    public ResponseEntity<List<SalesDetailDTO>> getCancelledSalesByShop(@PathVariable Integer shopCode) {
        return handleApiCall("샵코드 " + shopCode + "의 취소된 매출 조회",
            () -> salesService.getCancelledSalesDetailsByShop(shopCode));
    }

    /**
     * 특정 샵의 매출 상세 정보 조회 (단일 매출)
     * @param shopCode 샵 코드
     * @param salesCode 매출 코드
     * @return 매출 상세 정보
     */
    @GetMapping("/my-shops/{shopCode}/sales/{salesCode}")
    public ResponseEntity<SalesDetailDTO> getSalesDetailByCode(@PathVariable Integer shopCode,
        @PathVariable Integer salesCode) {
        return handleApiCall("샵코드 " + shopCode + ", 매출코드 " + salesCode + "의 상세 조회",
            () -> salesService.getSalesDetail(salesCode));
    }

    /**
     * 매출 통계 대시보드용 종합 정보 조회
     * @param shopCode 샵 코드
     * @param startDate 시작 날짜 (선택사항)
     * @param endDate 종료 날짜 (선택사항)
     * @return 통계 종합 정보
     */
    @GetMapping("/my-shops/{shopCode}/sales/dashboard")
    public ResponseEntity<SalesDashboardDTO> getDashboardStats(@PathVariable Integer shopCode,
        @RequestParam(required = false) String startDate,
        @RequestParam(required = false) String endDate) {
        LocalDateTime start = null;
        LocalDateTime end = null;
        if (startDate != null && endDate != null) {
            start = parseDateTime(startDate);
            end = parseDateTime(endDate);
        }
        LocalDateTime finalStart = start;
        LocalDateTime finalEnd = end;

        return handleApiCall("샵코드 " + shopCode + "의 대시보드 통계 조회",
            () -> salesService.getDashboardStats(shopCode, finalStart, finalEnd));
    }

    // ========== DTO 변환 헬퍼 메서드 ==========

    /**
     * SalesDetailDTO를 SalesDTO로 변환
     * @param salesDetailDTO 변환할 SalesDetailDTO
     * @param shopCode 샵 코드
     * @return 변환된 SalesDTO
     */
    private SalesDTO convertToSalesDTO(SalesDetailDTO salesDetailDTO, Integer shopCode) {
        SalesDTO salesDTO = new SalesDTO();

        // Sales 관련 필드 복사
        salesDTO.setSalesCode(salesDetailDTO.getSalesCode());
        salesDTO.setResvCode(salesDetailDTO.getResvCode());
        salesDTO.setPayAmount(salesDetailDTO.getPayAmount());
        salesDTO.setPayMethod(salesDetailDTO.getPayMethod());
        salesDTO.setPayDatetime(salesDetailDTO.getPayDatetime());
        salesDTO.setPayStatus(salesDetailDTO.getPayStatus());
        salesDTO.setCancelAmount(salesDetailDTO.getCancelAmount());
        salesDTO.setCancelDatetime(salesDetailDTO.getCancelDatetime());
        salesDTO.setCancelReason(salesDetailDTO.getCancelReason());
        salesDTO.setFinalAmount(salesDetailDTO.getFinalAmount());

        // 기본값 설정
        if (salesDTO.getPayDatetime() == null) {
            salesDTO.setPayDatetime(LocalDateTime.now());
        }
        if (salesDTO.getPayStatus() == null) {
            salesDTO.setPayStatus(com.header.header.domain.sales.enums.PaymentStatus.COMPLETED);
        }
        if (salesDTO.getCancelAmount() == null) {
            salesDTO.setCancelAmount(0);
        }
        if (salesDTO.getFinalAmount() == null) {
            salesDTO.setFinalAmount(salesDTO.getPayAmount());
        }

        return salesDTO;
    }
}