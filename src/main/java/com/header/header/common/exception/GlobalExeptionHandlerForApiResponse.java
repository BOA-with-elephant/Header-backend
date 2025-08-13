package com.header.header.common.exception;

import com.header.header.auth.exception.DuplicatedPhoneException;
import com.header.header.auth.exception.DuplicatedUserIdException;
import com.header.header.auth.exception.RegistrationUnknownException;
import com.header.header.common.dto.ResponseDTO;
import com.header.header.common.dto.response.ApiResponse;
import com.header.header.domain.reservation.enums.UserReservationErrorCode;
import com.header.header.domain.reservation.exception.UserReservationExceptionHandler;
import com.header.header.domain.shop.enums.ShopErrorCode;
import com.header.header.domain.shop.enums.ShopHolidayErrorCode;
import com.header.header.domain.shop.exception.ShopExceptionHandler;
import com.header.header.domain.shop.exception.ShopHolidayExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import javax.security.auth.login.FailedLoginException;

/**
 * 전역 예외 처리기
 *
 * - 전역 모든 예외를 통합 관리
 * - NotFoundException을 주로 사용하여 리소스 찾기 실패 처리
 * - 적절한 HTTP 상태 코드와 함께 구조화된 에러 정보 반환
 */
@RestControllerAdvice(basePackages = {
        "com.header.header.domain.visitors",
        "com.header.header.domain.message",
        "com.header.header.domain.user",
        "com.header.header.domain.shop",
        "com.header.header.domain.reservation"
})
@Slf4j
public class GlobalExeptionHandlerForApiResponse {

    /*
    * ShopException 처리
    *
    * - Shop 도메인의 예외 처리 시 발생
    * - ShopErrorCode enum에 처리해둔 HttpStatus에 따라 응답
    * */
    @ExceptionHandler(ShopExceptionHandler.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleShopExceptionHandler(
            ShopExceptionHandler ex, WebRequest request
    ){
        log.error("ShopException 발생: {} - {} ", ex.getShopErrorCode(), ex.getMessage(), ex);
        ShopErrorCode errorCode = ex.getShopErrorCode();
        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode.getHttpStatus().value(),
                String.valueOf(errorCode.getCode()),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(errorCode.getHttpStatus()).body(ApiResponse.fail(ex.getMessage(), errorResponse));
    }

    /*
     * ShopHolidayException 처리
     *
     * - Shop 도메인 - Shop Holiday의 예외 처리 시 발생
     * - ShopHolidayErrorCode enum에 처리해둔 HttpStatus에 따라 응답
     * */
    @ExceptionHandler(ShopHolidayExceptionHandler.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleShopHolidayExceptionHandler(
            ShopHolidayExceptionHandler ex, WebRequest request
    ){
        log.error("ShopHolidayException 발생: {} - {} ", ex.getShopHolidayErrorCode(), ex.getMessage(), ex);
        ShopHolidayErrorCode errorCode = ex.getShopHolidayErrorCode();

        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode.getHttpStatus().value(),
                String.valueOf(errorCode.getCode()),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(errorCode.getHttpStatus()).body(ApiResponse.fail(ex.getMessage(), errorResponse));
    }

    /*
     * UserReservationException 처리
     *
     * - UserReservation 도메인의 예외 처리 시 발생
     * - UserReservationErrorCode enum에 처리해둔 HttpStatus에 따라 응답
     * */
    @ExceptionHandler(UserReservationExceptionHandler.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleUserReservationExceptionHandler(
            UserReservationExceptionHandler ex, WebRequest request
    ){
        log.error("UserReservationException 발생: {} - {} ", ex.getURErrorCode(), ex.getMessage(), ex);
        UserReservationErrorCode errorCode = ex.getURErrorCode();
        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode.getHttpStatus().value(),
                String.valueOf(errorCode.getCode()),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(errorCode.getHttpStatus()).body(ApiResponse.fail(ex.getMessage(), errorResponse));
    }

    /**
     * NotFoundException 처리
     *
     * - 리소스를 찾을 수 없을 때 발생
     * - HTTP 404 Not Found 상태로 응답
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleNotFoundException(
            NotFoundException ex, WebRequest request) {

        log.error("리소스를 찾을 수 없음: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.notFound(
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.fail(errorResponse.getMessage(),errorResponse));
    }

    /**
     * IllegalArgumentException 처리
     *
     * - Spring 에서 기본적으로 발생하는 잘못된 매개변수 예외
     * - 유효성 검사 실패나 잘못된 값 입력 시 사용
     * - HTTP 400 Bad Request 상태로 응답
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleIllegalArgumentException(
            IllegalArgumentException ex, WebRequest request) {

        log.error("잘못된 매개변수: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.badRequest(
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail(errorResponse.getMessage(),errorResponse));
    }

    /**
     * 예상하지 못한 모든 예외 처리
     *
     * - 위에서 처리되지 않은 모든 예외를 포괄적으로 처리
     * - HTTP 500 Internal Server Error 상태로 응답
     * - 상세한 에러 정보는 로그에만 기록하고, 클라이언트에는 일반적인 메시지만 전달
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleGlobalException(
            Exception ex, WebRequest request) {

        log.error("예상하지 못한 오류 발생: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.internalServerError(
                "서버 내부 오류가 발생했습니다.",
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.fail(errorResponse.getMessage(),errorResponse));
    }

    /**
     * AuthUserService에서 비밀번호 불일치 시 발생하는 예외 상황 처리
     *
     * - HTTP 401 Unauthorized error 상태로 응답*/
    @ExceptionHandler(FailedLoginException.class)
    public ResponseEntity<ResponseDTO> handleFailedLoginException(FailedLoginException e) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED) //401
                .body(new ResponseDTO(HttpStatus.UNAUTHORIZED, e.getMessage(), null));
    }

    // DuplicatedUserIdException 처리 (409 Conflict)
    @ExceptionHandler(DuplicatedUserIdException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatedUserIdException(DuplicatedUserIdException ex, WebRequest request) {
        log.warn("DuplicatedUserIdException 발생: {}", ex.getMessage());
        // ErrorResponse의 of() 팩토리 메소드 사용
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(), // "Conflict"
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "") // 요청 경로 추출
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // DuplicatedPhoneException 처리 (409 Conflict)
    @ExceptionHandler(DuplicatedPhoneException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatedPhoneException(DuplicatedPhoneException ex, WebRequest request) {
        log.warn("DuplicatedPhoneException 발생: {}", ex.getMessage());
        // ErrorResponse의 of() 팩토리 메소드 사용
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // RegistrationUnknownException 처리 (500 Internal Server Error)
    @ExceptionHandler(RegistrationUnknownException.class)
    public ResponseEntity<ErrorResponse> handleRegistrationUnknownException(RegistrationUnknownException ex, WebRequest request) {
        log.error("RegistrationUnknownException 발생: {}", ex.getMessage(), ex);
        // ErrorResponse의 internalServerError() 팩토리 메소드 사용
        ErrorResponse errorResponse = ErrorResponse.internalServerError(
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
