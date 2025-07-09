package com.header.header.domain.menu.exception;

/**
 * 메뉴, 메뉴 카테고리 등 리소스를 찾을 수 없을 때 발생하는 공통 예외
 * - 메뉴 도메인뿐만 아니라 다른 도메인에서도 재사용 가능
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
}