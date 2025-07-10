package com.header.header.domain.sales.service;

import com.header.header.common.exception.NotFoundException;
import com.header.header.domain.sales.dto.SalesDTO;
import com.header.header.domain.sales.dto.SalesDetailDTO;
import com.header.header.domain.sales.entity.Sales;
import com.header.header.domain.sales.enums.PaymentStatus;
import com.header.header.domain.sales.repository.SalesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SalesService {

    private final SalesRepository salesRepository;
    private final ModelMapper modelMapper;

    /**
     * 새로운 결제 생성
     * @param salesDTO 결제 정보
     * @return 생성된 결제 정보
     * @throws IllegalArgumentException 중복 결제 시
     */
    @Transactional
    public SalesDTO createPayment(SalesDTO salesDTO) {
        log.info("결제 생성 시작 - resvCode: {}, amount: {}", salesDTO.getResvCode(), salesDTO.getPayAmount());

        // 기본값 설정
        if (salesDTO.getPayStatus() == null) {
            salesDTO.setPayStatus(PaymentStatus.COMPLETED);
        }
        if (salesDTO.getCancelAmount() == null) {
            salesDTO.setCancelAmount(0);
        }
        if (salesDTO.getPayDatetime() == null) {
            salesDTO.setPayDatetime(LocalDateTime.now());
        }
        if (salesDTO.getFinalAmount() == null) {
            salesDTO.setFinalAmount(salesDTO.getPayAmount());
        }

        // 중복 결제 검증
        validateDuplicatePayment(salesDTO.getResvCode());

        // Entity로 변환 후 저장
        Sales sales = toEntity(salesDTO);
        Sales savedSales = salesRepository.save(sales);

        log.info("결제 생성 완료 - salesCode: {}", savedSales.getSalesCode());
        return toDTO(savedSales);
    }

    /**
     * 매출 코드로 매출 상세 정보 조회
     * @param salesCode 매출 코드
     * @return 매출 상세 정보
     * @throws NotFoundException 매출을 찾을 수 없을 때
     */
    public SalesDetailDTO getSalesDetail(Integer salesCode) {
        return salesRepository.findSalesDetailById(salesCode)
            .orElseThrow(() -> NotFoundException.sales(salesCode));
    }

    /**
     * 예약 코드로 매출 상세 정보 조회
     * @param resvCode 예약 코드
     * @return 매출 상세 정보
     * @throws NotFoundException 매출을 찾을 수 없을 때
     */
    public SalesDetailDTO getSalesDetailByReservation(Integer resvCode) {
        return salesRepository.findSalesDetailByResvCode(resvCode)
            .orElseThrow(() -> NotFoundException.salesByReservation(resvCode));
    }

    /**
     * 특정 샵의 모든 매출 상세 목록 조회
     * @param shopCode 샵 코드
     * @return 매출 상세 목록
     */
    public List<SalesDetailDTO> getSalesDetailsByShop(Integer shopCode) {
        return salesRepository.findSalesDetailsByShop(shopCode);
    }

    /**
     * 특정 샵의 특정 상태 매출 상세 목록 조회
     * @param shopCode 샵 코드
     * @param status 결제 상태
     * @return 매출 상세 목록
     */
    public List<SalesDetailDTO> getSalesDetailsByShopAndStatus(Integer shopCode, PaymentStatus status) {
        return salesRepository.findSalesDetailsByShopAndStatus(shopCode, status);
    }

    /**
     * 특정 샵의 완료된 매출만 조회
     * @param shopCode 샵 코드
     * @return 완료된 매출 목록
     */
    public List<SalesDetailDTO> getCompletedSalesDetailsByShop(Integer shopCode) {
        return getSalesDetailsByShopAndStatus(shopCode, PaymentStatus.COMPLETED);
    }

    /**
     * 특정 샵의 취소된 매출만 조회 (전체취소 + 부분취소)
     * @param shopCode 샵 코드
     * @return 취소된 매출 목록
     */
    public List<SalesDetailDTO> getCancelledSalesDetailsByShop(Integer shopCode) {
        List<SalesDetailDTO> result = new ArrayList<>();
        result.addAll(getSalesDetailsByShopAndStatus(shopCode, PaymentStatus.CANCELLED));
        result.addAll(getSalesDetailsByShopAndStatus(shopCode, PaymentStatus.PARTIAL_CANCELLED));
        return result;
    }

    /**
     * 특정 샵의 기간별 매출 상세 목록 조회
     * @param shopCode 샵 코드
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 기간별 매출 목록
     */
    public List<SalesDetailDTO> getSalesDetailsByShopAndDateRange(Integer shopCode, LocalDateTime startDate, LocalDateTime endDate) {
        return salesRepository.findSalesDetailsByShopAndDateRange(shopCode, startDate, endDate);
    }

    /**
     * 결제 정보 수정
     * @param salesCode 매출 코드
     * @param salesDTO 수정할 결제 정보
     * @return 수정된 결제 정보
     * @throws NotFoundException 매출을 찾을 수 없을 때
     */
    @Transactional
    public SalesDTO updatePayment(Integer salesCode, SalesDTO salesDTO) {
        log.info("결제 수정 시작 - salesCode: {}", salesCode);

        Sales sales = salesRepository.findById(salesCode)
            .orElseThrow(() -> NotFoundException.sales(salesCode));

        sales.updatePaymentInfo(salesDTO.getPayMethod(), salesDTO.getCancelReason());

        log.info("결제 수정 완료 - salesCode: {}", salesCode);
        return toDTO(sales);
    }

    /**
     * 결제 취소 처리
     * @param salesCode 매출 코드
     * @param cancelAmount 취소 금액
     * @param cancelReason 취소 사유
     * @return 취소 처리된 결제 정보
     * @throws NotFoundException 매출을 찾을 수 없을 때
     * @throws IllegalStateException 취소 불가능한 상태일 때
     * @throws IllegalArgumentException 취소 금액이 유효하지 않을 때
     */
    @Transactional
    public SalesDTO cancelPayment(Integer salesCode, Integer cancelAmount, String cancelReason) {
        log.info("결제 취소 시작 - salesCode: {}, cancelAmount: {}", salesCode, cancelAmount);

        Sales sales = salesRepository.findById(salesCode)
            .orElseThrow(() -> NotFoundException.sales(salesCode));

        SalesDTO salesDTO = toDTO(sales);

        // 현재 상태로 취소 가능 여부 확인
        if (!salesDTO.getPayStatus().isCancellable()) {
            throw new IllegalStateException("현재 상태에서는 취소할 수 없습니다: " + salesDTO.getPayStatus().name());
        }

        // 취소 금액 검증
        validateCancelAmount(salesDTO, cancelAmount);

        // 새로운 상태 결정
        PaymentStatus newStatus = determinePaymentStatus(salesDTO.getPayAmount(),
            salesDTO.getCancelAmount() + cancelAmount);
        Integer newFinalAmount = salesDTO.getPayAmount() - (salesDTO.getCancelAmount() + cancelAmount);

        // 취소 처리 (Entity 메서드 사용)
        sales.processCancelation(
            salesDTO.getCancelAmount() + cancelAmount, // 누적 취소 금액
            cancelReason,
            newStatus, // PaymentStatus enum 직접 전달
            newFinalAmount
        );

        log.info("결제 취소 완료 - salesCode: {}, status: {}", salesCode, newStatus.name());
        return toDTO(sales);
    }

    /**
     * 매출 논리적 삭제 (상태를 DELETED로 변경)
     * @param salesCode 매출 코드
     * @throws NotFoundException 매출을 찾을 수 없을 때
     */
    @Transactional
    public void deleteSales(Integer salesCode) {
        log.info("결제 삭제 시작 - salesCode: {}", salesCode);

        Sales sales = salesRepository.findById(salesCode)
            .orElseThrow(() -> NotFoundException.sales(salesCode));

        sales.updatePaymentStatus(PaymentStatus.DELETED);

        log.info("결제 삭제 완료 - salesCode: {}", salesCode);
    }

    /**
     * 기간별 매출 조회
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 기간별 매출 목록
     */
    public List<SalesDTO> getSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Sales> salesList = salesRepository.findByPayDatetimeBetween(startDate, endDate);
        return toDTOList(salesList);
    }

    // === 추가 편의 메서드들 ===

    /**
     * 특정 샵의 매출 통계 조회 (삭제된 매출 제외)
     * @param shopCode 샵 코드
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 총 매출 금액
     */
    public Long calculateTotalSales(Integer shopCode, LocalDateTime startDate, LocalDateTime endDate) {
        Long total = salesRepository.calculateTotalSalesByShopBetween(shopCode, startDate, endDate, PaymentStatus.DELETED);
        return total != null ? total : 0L;
    }

    /**
     * 특정 샵의 취소 금액 통계 조회
     * @param shopCode 샵 코드
     * @param startDate 시작 날짜
     * @param endDate 종료 날짜
     * @return 총 취소 금액
     */
    public Long calculateTotalCancelAmount(Integer shopCode, LocalDateTime startDate, LocalDateTime endDate) {
        Long total = salesRepository.calculateTotalCancelAmountByShopBetween(shopCode, startDate, endDate);
        return total != null ? total : 0L;
    }

    /**
     * 특정 샵의 결제 방법별 통계 조회
     * @param shopCode 샵 코드
     * @return 결제 방법별 통계 [결제방법, 총금액, 건수]
     */
    public List<Object[]> getSalesStatsByPayMethod(Integer shopCode) {
        return salesRepository.findSalesStatsByPayMethodAndShop(shopCode, PaymentStatus.DELETED);
    }

    /**
     * 특정 샵의 월별 매출 통계 조회
     * @param shopCode 샵 코드
     * @return 월별 통계 [년도, 월, 총금액, 건수]
     */
    public List<Object[]> getMonthlySalesStats(Integer shopCode) {
        return salesRepository.findMonthlySalesStatsByShop(shopCode, PaymentStatus.DELETED);
    }

    /**
     * 특정 샵의 활성 결제 조회 (삭제되지 않은 결제)
     * @param shopCode 샵 코드
     * @return 활성 결제 목록
     */
    public List<SalesDTO> getActivePaymentsByShop(Integer shopCode) {
        List<Sales> salesList = salesRepository.findActivePaymentsByShop(shopCode, PaymentStatus.DELETED);
        return toDTOList(salesList);
    }

    /**
     * 특정 샵의 고액 결제 조회
     * @param shopCode 샵 코드
     * @param amount 최소 금액
     * @return 고액 결제 목록
     */
    public List<SalesDTO> getHighAmountPaymentsByShop(Integer shopCode, Integer amount) {
        List<Sales> salesList = salesRepository.findHighAmountPaymentsByShop(shopCode, amount, PaymentStatus.DELETED);
        return toDTOList(salesList);
    }

    /**
     * 취소 가능한 상태인지 확인
     * @param salesDTO 매출 정보
     * @return 취소 가능 여부
     */
    public boolean canCancel(SalesDTO salesDTO) {
        return salesDTO.getPayStatus().isCancellable();
    }

    /**
     * 취소된 상태인지 확인
     * @param salesDTO 매출 정보
     * @return 취소 상태 여부
     */
    public boolean isCancelled(SalesDTO salesDTO) {
        return salesDTO.getPayStatus().isCancelled();
    }

    // === Private 메서드들 ===

    /**
     * 중복 결제 검증
     * @param resvCode 예약 코드
     * @throws IllegalArgumentException 중복 결제 시
     */
    private void validateDuplicatePayment(Integer resvCode) {
        if (salesRepository.existsByResvCode(resvCode)) {
            throw new IllegalArgumentException("이미 해당 예약에 대한 결제가 존재합니다. 예약코드: " + resvCode);
        }
    }

    /**
     * 취소 금액의 유효성 검증
     * @param salesDTO 매출 정보
     * @param cancelAmount 취소 금액
     * @throws IllegalArgumentException 유효하지 않은 취소 금액일 때
     */
    private void validateCancelAmount(SalesDTO salesDTO, Integer cancelAmount) {
        if (cancelAmount <= 0) {
            throw new IllegalArgumentException("취소 금액은 0보다 커야 합니다.");
        }
        Integer remainingAmount = salesDTO.getPayAmount() - salesDTO.getCancelAmount();
        if (cancelAmount > remainingAmount) {
            throw new IllegalArgumentException("취소 금액이 남은 금액을 초과합니다. 남은 금액: " + remainingAmount);
        }
    }

    /**
     * 취소 금액에 따라 결제 상태 결정
     * @param payAmount 결제 금액
     * @param totalCancelAmount 총 취소 금액
     * @return 새로운 결제 상태
     */
    private PaymentStatus determinePaymentStatus(Integer payAmount, Integer totalCancelAmount) {
        return totalCancelAmount.equals(payAmount) ? PaymentStatus.CANCELLED : PaymentStatus.PARTIAL_CANCELLED;
    }

    // === 공통 변환 로직 ===

    /**
     * 엔티티 → DTO 변환
     * @param sales Sales 엔티티
     * @return SalesDTO
     */
    private SalesDTO toDTO(Sales sales) {
        return modelMapper.map(sales, SalesDTO.class);
    }

    /**
     * DTO → 엔티티 변환
     * @param salesDTO SalesDTO
     * @return Sales 엔티티
     */
    private Sales toEntity(SalesDTO salesDTO) {
        return modelMapper.map(salesDTO, Sales.class);
    }

    /**
     * 엔티티 리스트 → DTO 리스트 변환
     * @param salesList Sales 엔티티 리스트
     * @return SalesDTO 리스트
     */
    private List<SalesDTO> toDTOList(List<Sales> salesList) {
        return salesList.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
}