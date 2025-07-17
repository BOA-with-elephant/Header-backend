package com.header.header.common.exception;

import com.header.header.domain.shop.common.ErrorResponseMessage;
import com.header.header.domain.shop.common.ResponseMessage;
import com.header.header.domain.shop.enums.ShopErrorCode;
import com.header.header.domain.shop.exception.ShopExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

/**
 * 전역 예외 처리기
 *
 * - 전역 모든 예외를 통합 관리
 * - NotFoundException을 주로 사용하여 리소스 찾기 실패 처리
 * - 적절한 HTTP 상태 코드와 함께 구조화된 에러 정보 반환
 */
@RestControllerAdvice(basePackages = "com.header.header")
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ShopExceptionHandler.class)
    public ResponseEntity<ResponseMessage> handleShopException(ShopExceptionHandler e) {
        ShopErrorCode errorInfo = e.getShopErrorCode();

        ErrorResponseMessage error = new ErrorResponseMessage(errorInfo.getCode(), errorInfo.getMessage());

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("error", error);

        ResponseMessage responseMessage = new ResponseMessage(400, "잘못된 요청 발생", responseMap);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new ResponseEntity<>(responseMessage, headers, HttpStatus.BAD_REQUEST);
    }

    /**
     * NotFoundException 처리
     *
     * - 리소스를 찾을 수 없을 때 발생
     * - HTTP 404 Not Found 상태로 응답
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(
        NotFoundException ex, WebRequest request) {

        log.error("리소스를 찾을 수 없음: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.notFound(
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * IllegalArgumentException 처리
     *
     * - Spring 에서 기본적으로 발생하는 잘못된 매개변수 예외
     * - 유효성 검사 실패나 잘못된 값 입력 시 사용
     * - HTTP 400 Bad Request 상태로 응답
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
        IllegalArgumentException ex, WebRequest request) {

        log.error("잘못된 매개변수: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.badRequest(
            ex.getMessage(),
            request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * 예상하지 못한 모든 예외 처리
     *
     * - 위에서 처리되지 않은 모든 예외를 포괄적으로 처리
     * - HTTP 500 Internal Server Error 상태로 응답
     * - 상세한 에러 정보는 로그에만 기록하고, 클라이언트에는 일반적인 메시지만 전달
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(
        Exception ex, WebRequest request) {

        if (ex instanceof ShopExceptionHandler) {
            throw (ShopExceptionHandler) ex;
        }

        log.error("예상하지 못한 오류 발생: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.internalServerError(
            "서버 내부 오류가 발생했습니다.",
            request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}