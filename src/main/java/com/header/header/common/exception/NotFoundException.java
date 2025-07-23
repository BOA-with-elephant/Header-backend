package com.header.header.common.exception;

import com.header.header.domain.sales.enums.PaymentStatus;

/**
 * 전역에서 리소스를 찾을 수 없을 때 발생하는 공통 예외
 * - HTTP 404 Not Found 상태로 응답
 */
public class NotFoundException extends RuntimeException {

    /**
     * 기본 메시지로 예외 생성
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * 코드로 기본 메시지 생성
     */
    public NotFoundException(Integer code) {
        super("해당 정보를 찾을 수 없습니다. 코드: " + code);
    }

    /**
     * 엔티티명과 코드로 메시지 생성
     */
    public NotFoundException(String entity, Integer code) {
        super(entity + "를 찾을 수 없습니다. 코드: " + code);
    }

    /**
     * 엔티티명과 추가 정보로 메시지 생성
     */
    public NotFoundException(String entity, String additionalInfo) {
        super(entity + "를 찾을 수 없습니다. " + additionalInfo);
    }

    // === 편의 팩토리 메소드들 (메뉴 도메인) ===

    /**
     * 메뉴를 찾을 수 없을 때 사용하는 팩토리 메소드
     */
    public static NotFoundException menu(Integer menuCode) {
        return new NotFoundException("메뉴", menuCode);
    }

    /**
     * 메뉴 카테고리를 찾을 수 없을 때 사용하는 팩토리 메소드
     */
    public static NotFoundException category(Integer categoryCode, Integer shopCode) {
        return new NotFoundException("메뉴 카테고리", "카테고리 코드: " + categoryCode + ", 샵 코드: " + shopCode);
    }

    // === 편의 팩토리 메소드들 (매출 도메인) ===

    /**
     * 매출을 찾을 수 없을 때 사용하는 팩토리 메소드
     */
    public static NotFoundException sales(Integer salesCode) {
        return new NotFoundException("매출", salesCode);
    }

    /**
     * 예약의 매출을 찾을 수 없을 때 사용하는 팩토리 메소드
     */
    public static NotFoundException salesByReservation(Integer resvCode) {
        return new NotFoundException("매출", "예약 코드: " + resvCode);
    }

    /**
     * 특정 상태의 매출을 찾을 수 없을 때 사용하는 팩토리 메소드
     */
    public static NotFoundException salesByStatus(Integer shopCode, PaymentStatus status) {
        return new NotFoundException("매출",
            "샵 코드: " + shopCode + ", 결제 상태: " + status.name());
    }

    /**
     * 특정 샵의 매출을 찾을 수 없을 때 사용하는 팩토리 메소드
     */
    public static NotFoundException salesByShop(Integer shopCode) {
        return new NotFoundException("매출", "샵 코드: " + shopCode);
    }

    /**
     * 특정 기간의 매출을 찾을 수 없을 때 사용하는 팩토리 메소드
     */
    public static NotFoundException salesByDateRange(String startDate, String endDate) {
        return new NotFoundException("매출",
            "조회 기간: " + startDate + " ~ " + endDate);
    }

    /**
     * 특정 결제 방법의 매출을 찾을 수 없을 때 사용하는 팩토리 메소드
     */
    public static NotFoundException salesByPayMethod(String payMethod) {
        return new NotFoundException("매출", "결제 방법: " + payMethod);
    }

    /**
     * 특정 금액 범위의 매출을 찾을 수 없을 때 사용하는 팩토리 메소드
     */
    public static NotFoundException salesByAmountRange(Integer minAmount, Integer maxAmount) {
        return new NotFoundException("매출",
            "금액 범위: " + minAmount + "원 ~ " + maxAmount + "원");
    }

    /**
     * 취소된 매출을 찾을 수 없을 때 사용하는 팩토리 메소드
     */
    public static NotFoundException cancelledSales(Integer shopCode) {
        return new NotFoundException("취소된 매출", "샵 코드: " + shopCode);
    }

    /**
     * 완료된 매출을 찾을 수 없을 때 사용하는 팩토리 메소드
     */
    public static NotFoundException completedSales(Integer shopCode) {
        return new NotFoundException("완료된 매출", "샵 코드: " + shopCode);
    }

    /**
     * 활성 매출(삭제되지 않은)을 찾을 수 없을 때 사용하는 팩토리 메소드
     */
    public static NotFoundException activeSales(Integer shopCode) {
        return new NotFoundException("활성 매출", "샵 코드: " + shopCode);
    }

    // === 편의 팩토리 메소드들 (예약 도메인) ===

    /**
     * 예약을 찾을 수 없을 때 사용하는 팩토리 메소드
     */
    public static NotFoundException reservation(Integer resvCode) {
        return new NotFoundException("예약", resvCode);
    }

    /**
     * 특정 샵의 예약을 찾을 수 없을 때 사용하는 팩토리 메소드
     */
    public static NotFoundException reservationByShop(Integer shopCode) {
        return new NotFoundException("예약", "샵 코드: " + shopCode);
    }

    /**
     * 특정 사용자의 예약을 찾을 수 없을 때 사용하는 팩토리 메소드
     */
    public static NotFoundException reservationByUser(Integer userCode) {
        return new NotFoundException("예약", "사용자 코드: " + userCode);
    }

    // === 편의 팩토리 메소드들 (사용자 도메인) ===

    /**
     * 사용자를 찾을 수 없을 때 사용하는 팩토리 메소드
     */
    public static NotFoundException user(Integer userCode) {
        return new NotFoundException("사용자", userCode);
    }

    /**
     * 이메일로 사용자를 찾을 수 없을 때 사용하는 팩토리 메소드
     */
    public static NotFoundException userByEmail(String email) {
        return new NotFoundException("사용자", "이메일: " + email);
    }

    // === 편의 팩토리 메소드들 (샵 도메인) ===

    /**
     * 샵을 찾을 수 없을 때 사용하는 팩토리 메소드
     */
    public static NotFoundException shop(Integer shopCode) {
        return new NotFoundException("샵", shopCode);
    }

    /**
     * 샵명으로 샵을 찾을 수 없을 때 사용하는 팩토리 메소드
     */
    public static NotFoundException shopByName(String shopName) {
        return new NotFoundException("샵", "샵명: " + shopName);
    }

    // 필요시, 각 도메인에 맞는 팩토리 메소드 추가 생성하세요~!
}