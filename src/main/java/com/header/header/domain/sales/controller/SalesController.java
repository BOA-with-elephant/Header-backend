package com.header.header.domain.sales.controller;

import com.header.header.domain.sales.dto.SalesDTO;
import com.header.header.domain.sales.service.SalesService;
import com.header.header.domain.reservation.service.BossReservationService;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
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
@RequestMapping(value = "/api/v1", produces = "application/json; charset=UTF-8")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"},
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class SalesController {

    /**
     * 에러 응답 객체 생성
     */
    private Map<String, Object> createErrorResponse(String error, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", error);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        return errorResponse;
    }

    /**
     * 유효성 검증 에러 응답 생성
     */
    private Map<String, Object> createValidationErrorResponse(BindingResult bindingResult) {
        List<String> errors = bindingResult.getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", "Validation Failed");
        errorResponse.put("message", "유효성 검증에 실패했습니다.");
        errorResponse.put("details", errors);
        errorResponse.put("timestamp", LocalDateTime.now().toString());

        return errorResponse;
    }

    /**
     * SalesDTO 기본값 설정
     */
    private void setDefaultValues(SalesDTO salesDTO) {
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
    }

    // ========== 매출 등록/수정/삭제 API ==========

    /**
     * 새로운 매출 등록 (SalesDTO 직접 사용, 유효성 검증 강화)
     * @param shopCode 매출을 등록할 샵 코드
     * @param salesDTO 등록할 매출 정보
     * @param bindingResult 유효성 검증 결과
     * @return 등록된 매출 정보 또는 에러 정보
     */
    @PostMapping("/my-shops/{shopCode}/sales")
    public ResponseEntity<?> createSales(@PathVariable Integer shopCode,
        @Valid @RequestBody SalesDTO salesDTO,
        BindingResult bindingResult) {

        // 유효성 검증 실패 시 상세한 에러 메시지 반환
        if (bindingResult.hasErrors()) {
            Map<String, Object> errorResponse = createValidationErrorResponse(bindingResult);
            log.error("매출 등록 유효성 검증 실패: {}", errorResponse.get("details"));
            return ResponseEntity.badRequest().body(errorResponse);
        }

        return handleApiCall("샵코드 " + shopCode + "의 새로운 매출 등록 요청", () -> {

            // 예약 기반 매출 등록인 경우
            if (salesDTO.getResvCode() != null) {
                log.info("예약 기반 매출 등록 - 예약코드: {}", salesDTO.getResvCode());

                try {
                    // BossReservationService의 afterProcedure 사용 (void 반환)
                    reservationService.afterProcedure(salesDTO);

                    log.info("예약 기반 매출 등록 완료 - 예약코드: {}", salesDTO.getResvCode());

                    // afterProcedure 내부에서 매출 등록이 완료되므로 성공 메시지 반환
                    return Map.of(
                        "message", "예약 기반 매출이 성공적으로 등록되었습니다.",
                        "resvCode", salesDTO.getResvCode(),
                        "shopCode", shopCode,
                        "status", "completed"
                    );

                } catch (IllegalArgumentException e) {
                    log.error("예약 기반 매출 등록 중 잘못된 요청 - 예약코드: {}, 오류: {}",
                        salesDTO.getResvCode(), e.getMessage());
                    throw e;
                } catch (IllegalStateException e) {
                    log.error("예약 기반 매출 등록 중 상태 오류 - 예약코드: {}, 오류: {}",
                        salesDTO.getResvCode(), e.getMessage());
                    throw e;
                } catch (Exception e) {
                    log.error("예약 기반 매출 등록 중 예상치 못한 오류 발생 - 예약코드: {}, 오류: {}",
                        salesDTO.getResvCode(), e.getMessage(), e);
                    throw new RuntimeException("예약 기반 매출 등록 실패: " + e.getMessage(), e);
                }

            } else {
                // 직접 매출 등록 (예약과 연결되지 않은 경우)
                log.info("예약 연결 없는 직접 매출 등록 - 고객: {}, 시술: {}",
                    salesDTO.getUserName(), salesDTO.getMenuName());

                // 기본값 자동 설정
                setDefaultValues(salesDTO);

                // 전송된 데이터 로깅 (디버깅용)
                log.info("매출 등록 데이터 - payAmount: {}, payMethod: {}, finalAmount: {}",
                    salesDTO.getPayAmount(), salesDTO.getPayMethod(), salesDTO.getFinalAmount());

                // 매출 등록 실행
                return salesService.createPayment(salesDTO);
            }
        });
    }
    /**
     * 기존 매출 수정 (SalesDTO 직접 사용)
     * @param shopCode 매출이 속한 샵 코드
     * @param salesCode 수정할 매출 코드
     * @param salesDTO 수정할 매출 정보
     * @param bindingResult 유효성 검증 결과
     * @return 수정된 매출 정보 또는 에러 정보
     */
    @PutMapping("/my-shops/{shopCode}/sales/{salesCode}")
    public ResponseEntity<?> updateSales(@PathVariable Integer shopCode,
        @PathVariable Integer salesCode,
        @Valid @RequestBody SalesDTO salesDTO,
        BindingResult bindingResult) {

        // 유효성 검증 실패 시 에러 반환
        if (bindingResult.hasErrors()) {
            Map<String, Object> errorResponse = createValidationErrorResponse(bindingResult);
            log.error("매출 수정 유효성 검증 실패: {}", errorResponse.get("details"));
            return ResponseEntity.badRequest().body(errorResponse);
        }

        return handleApiCall("샵코드 " + shopCode + ", 매출코드 " + salesCode + "의 매출 수정 요청", () -> {
            salesDTO.setSalesCode(salesCode);
            setDefaultValues(salesDTO);
            return salesService.updatePayment(salesCode, salesDTO);
        });
    }

    /**
     * 매출 취소 처리
     * @param shopCode 매출이 속한 샵 코드
     * @param salesCode 취소할 매출 코드
     * @param cancelRequest 취소 요청 정보 (cancelAmount, cancelReason 포함)
     * @return 취소 처리된 매출 정보 또는 에러 정보
     */
    @PutMapping("/my-shops/{shopCode}/sales/{salesCode}/cancel")
    public ResponseEntity<?> cancelSales(@PathVariable Integer shopCode,
        @PathVariable Integer salesCode,
        @RequestBody Map<String, Object> cancelRequest) {
        return handleApiCall("샵코드 " + shopCode + ", 매출코드 " + salesCode + "의 매출 취소 요청", () -> {
            Integer cancelAmount = Integer.valueOf(cancelRequest.get("cancelAmount").toString());
            String cancelReason = cancelRequest.get("cancelReason").toString();
            return salesService.cancelPayment(salesCode, cancelAmount, cancelReason);
        });
    }

    /**
     * 매출 삭제 (논리적 삭제)
     * @param shopCode 삭제할 매출이 속한 샵 코드
     * @param salesCode 삭제할 매출 코드
     * @return 삭제 완료 응답 또는 에러 정보
     */
    @DeleteMapping("/my-shops/{shopCode}/sales/{salesCode}")
    public ResponseEntity<?> deleteSales(@PathVariable Integer shopCode,
        @PathVariable Integer salesCode) {
        return handleApiCall("샵코드 " + shopCode + ", 매출코드 " + salesCode + "의 매출 삭제 요청", () -> {
            salesService.deleteSales(salesCode);
            return Map.of("message", "매출이 성공적으로 삭제되었습니다.", "salesCode", salesCode);
        });
    }

    // ========== 매출 조회 API ==========

    /**
     * 특정 샵의 전체 매출 조회 (삭제 제외)
     * @param shopCode 조회할 샵의 코드
     * @return 해당 샵의 모든 SalesDetail DTO 리스트
     */
    @GetMapping("/my-shops/{shopCode}/sales/active")
    public ResponseEntity<?> getActiveSalesByShop(@PathVariable Integer shopCode) {
        return handleApiCall("샵코드 " + shopCode + "의 활성 매출 조회 요청",
            () -> salesService.getActiveSalesDetailsByShop(shopCode));
    }

    /**
     * 특정 샵의 매출 상세 정보 조회 (단일 매출)
     * @param shopCode 샵 코드
     * @param salesCode 매출 코드
     * @return 매출 상세 정보
     */
    @GetMapping("/my-shops/{shopCode}/sales/{salesCode}")
    public ResponseEntity<?> getSalesDetailByCode(@PathVariable Integer shopCode,
        @PathVariable Integer salesCode) {
        return handleApiCall("샵코드 " + shopCode + ", 매출코드 " + salesCode + "의 상세 조회",
            () -> salesService.getSalesDetail(salesCode));
    }

    /**
     * 특정 샵의 완료된 매출만 조회
     * @param shopCode 샵 코드
     * @return 완료된 매출 목록
     */
    @GetMapping("/my-shops/{shopCode}/sales/completed")
    public ResponseEntity<?> getCompletedSalesByShop(@PathVariable Integer shopCode) {
        return handleApiCall("샵코드 " + shopCode + "의 완료된 매출 조회",
            () -> salesService.getCompletedSalesDetailsByShop(shopCode));
    }

    /**
     * 특정 샵의 취소된 매출만 조회
     * @param shopCode 샵 코드
     * @return 취소된 매출 목록 (전체취소 + 부분취소)
     */
    @GetMapping("/my-shops/{shopCode}/sales/cancelled")
    public ResponseEntity<?> getCancelledSalesByShop(@PathVariable Integer shopCode) {
        return handleApiCall("샵코드 " + shopCode + "의 취소된 매출 조회",
            () -> salesService.getCancelledSalesDetailsByShop(shopCode));
    }

    /**
     * 특정 샵의 기간별 매출 상세 조회
     * @param shopCode 샵 코드
     * @param startDate 시작 날짜 (ISO 형식: yyyy-MM-ddTHH:mm:ss)
     * @param endDate 종료 날짜 (ISO 형식: yyyy-MM-ddTHH:mm:ss)
     * @return 기간별 매출 상세 목록
     */
    @GetMapping("/my-shops/{shopCode}/sales/period")
    public ResponseEntity<?> getSalesInPeriod(@PathVariable Integer shopCode,
        @RequestParam String startDate,
        @RequestParam String endDate) {
        LocalDateTime start = parseDateTime(startDate);
        LocalDateTime end = parseDateTime(endDate);
        return handleApiCall("샵코드 " + shopCode + "의 기간별 매출 상세 조회",
            () -> salesService.getSalesDetailsByShopAndDateRange(shopCode, start, end));
    }

    // ========== 매출 통계 API ==========

    /**
     * 특정 샵의 기간별 총 매출 조회
     * @param shopCode 샵 코드
     * @param startDate 시작 날짜 (ISO 형식: yyyy-MM-ddTHH:mm:ss)
     * @param endDate 종료 날짜 (ISO 형식: yyyy-MM-ddTHH:mm:ss)
     * @return 총 매출 금액
     */
    @GetMapping("/my-shops/{shopCode}/sales/total-sales")
    public ResponseEntity<?> getTotalSales(@PathVariable Integer shopCode,
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
    public ResponseEntity<?> getTotalCancelAmount(@PathVariable Integer shopCode,
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
    public ResponseEntity<?> getPaymentMethodStats(@PathVariable Integer shopCode) {
        return handleApiCall("샵코드 " + shopCode + "의 결제 수단별 통계 조회",
            () -> salesService.getSalesStatsByPayMethod(shopCode));
    }

    /**
     * 특정 샵의 월별 매출 통계 조회
     * @param shopCode 샵 코드
     * @return 월별 통계 [년도, 월, 총금액, 건수]
     */
    @GetMapping("/my-shops/{shopCode}/sales/monthly-stats")
    public ResponseEntity<?> getMonthlySalesStats(@PathVariable Integer shopCode) {
        return handleApiCall("샵코드 " + shopCode + "의 월별 매출 통계 조회",
            () -> salesService.getMonthlySalesStats(shopCode));
    }
    private static final Logger log = LoggerFactory.getLogger(SalesController.class);
    private final SalesService salesService;

    private final BossReservationService reservationService; // 예약 서비스 추가

    public SalesController(SalesService salesService,
        @Autowired(required = false) BossReservationService reservationService) {
        this.salesService = salesService;
        this.reservationService = reservationService;
    }

    // ========== 공통 유틸리티 메서드 ==========

    /**
     * 날짜 문자열을 LocalDateTime으로 파싱
     */
    private LocalDateTime parseDateTime(String dateTimeStr) {
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
    }

    /**
     * API 호출을 공통으로 처리하는 헬퍼 메서드 (에러 처리 개선)
     */
    private <T> ResponseEntity<?> handleApiCall(String logMessage, Supplier<T> supplier) {
        try {
            log.debug(logMessage);
            T result = supplier.get();
            return ResponseEntity.ok(result);
        } catch (IllegalArgumentException e) {
            log.error(logMessage + " 중 잘못된 요청", e);
            Map<String, Object> errorResponse = createErrorResponse(
                "Bad Request",
                e.getMessage() != null ? e.getMessage() : "잘못된 요청입니다."
            );
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error(logMessage + " 중 오류 발생", e);
            Map<String, Object> errorResponse = createErrorResponse(
                "Internal Server Error",
                e.getMessage() != null ? e.getMessage() : "서버 내부 오류가 발생했습니다."
            );
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * 매출 통계 대시보드용 종합 정보 조회
     * @param shopCode 샵 코드
     * @param startDate 시작 날짜 (선택사항)
     * @param endDate 종료 날짜 (선택사항)
     * @return 통계 종합 정보
     */
    @GetMapping("/my-shops/{shopCode}/sales/dashboard")
    public ResponseEntity<?> getDashboardStats(@PathVariable Integer shopCode,
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
}