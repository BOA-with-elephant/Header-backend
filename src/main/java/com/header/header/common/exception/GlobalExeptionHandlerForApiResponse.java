package com.header.header.common.exception;

import com.header.header.auth.exception.*;
import com.header.header.domain.chatbot.exception.ChatbotException;
import com.header.header.domain.chatbot.exception.ChatbotErrorCode;
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
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ConstraintViolation;

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
        "com.header.header.domain.reservation",
        "com.header.header.domain.chatbot"
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
        ShopErrorCode errorCode = ex.getShopErrorCode();

        log.error("ShopException 발생: {} - {} ", errorCode, errorCode.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode.getHttpStatus().value(),
                errorCode.getCode(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(errorCode.getHttpStatus()).body(ApiResponse.fail(errorCode.getMessage(), errorResponse));
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
        ShopHolidayErrorCode errorCode = ex.getShopHolidayErrorCode();

        log.error("ShopHolidayException 발생: {} - {} ", errorCode, errorCode.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode.getHttpStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return ResponseEntity.status(errorCode.getHttpStatus()).body(ApiResponse.fail(errorCode.getMessage(), errorResponse));
    }

    /**
     * ChatbotException 처리
     *
     * - Chatbot 도메인의 예외 처리 시 발생
     * - FastAPI, OpenAI, Database 등 다양한 에러 유형별로 처리
     * - 사용자용 메시지와 개발자용 메시지 분리 제공
     */
    @ExceptionHandler(ChatbotException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleChatbotException(
            ChatbotException ex, WebRequest request) {

        String path = request.getDescription(false).replace("uri=", "");
        HttpStatus httpStatus = determineHttpStatusForChatbotError(ex.getErrorCode());
        
        // 개발자용 상세 로그 (에러 타입별로 다른 로그 레벨 적용)
        switch (ex.getErrorCode()) {
            case FASTAPI_CONNECTION_ERROR, FASTAPI_TIMEOUT_ERROR, FASTAPI_SERVER_ERROR, FASTAPI_INVALID_RESPONSE -> // FastAPI errors
                log.error("FastAPI 통신 오류: {} - 개발자 메시지: {}", ex.getErrorCode().getErrorCode(), ex.getDeveloperMessage(), ex);
            case OPENAI_API_ERROR, OPENAI_QUOTA_EXCEEDED, OPENAI_RATE_LIMIT -> // OpenAI errors  
                log.error("OpenAI API 오류: {} - 개발자 메시지: {}", ex.getErrorCode().getErrorCode(), ex.getDeveloperMessage(), ex);
            case DATABASE_CONNECTION_ERROR, DATABASE_QUERY_ERROR, SHOP_NOT_FOUND, CUSTOMER_DATA_ERROR -> // Database errors
                log.error("DB 연결 오류: {} - 개발자 메시지: {}", ex.getErrorCode().getErrorCode(), ex.getDeveloperMessage(), ex);
            case INVALID_REQUEST_FORMAT, MISSING_REQUIRED_FIELD, INVALID_SHOP_ID, EMPTY_MESSAGE, MESSAGE_TOO_LONG -> // Validation errors
                log.warn("요청 검증 실패: {} - 개발자 메시지: {}", ex.getErrorCode().getErrorCode(), ex.getDeveloperMessage());
            case REDIS_CONNECTION_ERROR, REDIS_STREAM_ERROR -> // Redis errors
                log.error("Redis 스트림 오류: {} - 개발자 메시지: {}", ex.getErrorCode().getErrorCode(), ex.getDeveloperMessage(), ex);
            default -> // General errors
                log.error("알 수 없는 챗봇 오류: {} - 개발자 메시지: {}", ex.getErrorCode().getErrorCode(), ex.getDeveloperMessage(), ex);
        }

        ErrorResponse errorResponse = ErrorResponse.of(
                httpStatus.value(),
                ex.getErrorCode().getErrorCode(),
                ex.getUserMessage(), // 사용자용 메시지
                path
        );

        // 개발자용 추가 정보는 별도 필드로 제공 (프로덕션에서는 제외 가능)
        if (isDevelopmentEnvironment()) {
            errorResponse = ErrorResponse.of(
                    httpStatus.value(),
                    ex.getErrorCode().getErrorCode(),
                    ex.getUserMessage() + " [DEV: " + ex.getDeveloperMessage() + "]",
                    path
            );
        }

        return ResponseEntity.status(httpStatus)
                .body(ApiResponse.fail(ex.getUserMessage(), errorResponse));
    }

    /**
     * Bean Validation (@Valid) 예외 처리
     * - @RequestBody에서 validation 실패 시 발생
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        StringBuilder errorMessage = new StringBuilder("입력 데이터 검증 실패: ");
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errorMessage.append(fieldError.getDefaultMessage()).append("; ");
        }
        
        log.warn("Validation error: {}", errorMessage.toString());
        
        ErrorResponse errorResponse = ErrorResponse.badRequest(
                errorMessage.toString(),
                request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(errorMessage.toString(), errorResponse));
    }

    /**
     * Path Variable/Request Parameter validation 예외 처리
     * - @PathVariable, @RequestParam에서 validation 실패 시 발생
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleConstraintViolationException(
            ConstraintViolationException ex, WebRequest request) {
        
        StringBuilder errorMessage = new StringBuilder("요청 파라미터 검증 실패: ");
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errorMessage.append(violation.getMessage()).append("; ");
        }
        
        log.warn("Constraint violation: {}", errorMessage.toString());
        
        ErrorResponse errorResponse = ErrorResponse.badRequest(
                errorMessage.toString(),
                request.getDescription(false).replace("uri=", "")
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(errorMessage.toString(), errorResponse));
    }

    /**
     * ChatbotErrorCode에 따른 적절한 HTTP 상태 코드 결정
     */
    private HttpStatus determineHttpStatusForChatbotError(ChatbotErrorCode errorCode) {
        String code = errorCode.getErrorCode();
        
        // FastAPI Communication Errors (001-004) -> 502 Bad Gateway
        if (code.startsWith("CHATBOT_00")) {
            if (code.equals("CHATBOT_002")) { // Timeout
                return HttpStatus.GATEWAY_TIMEOUT; // 504
            }
            return HttpStatus.BAD_GATEWAY; // 502
        }
        
        // OpenAI API Errors (101-103) -> 502 Bad Gateway or 429 Too Many Requests
        if (code.startsWith("CHATBOT_10")) {
            if (code.equals("CHATBOT_103")) { // Rate Limit
                return HttpStatus.TOO_MANY_REQUESTS; // 429
            }
            return HttpStatus.BAD_GATEWAY; // 502
        }
        
        // Database Errors (201-204) -> 503 Service Unavailable
        if (code.startsWith("CHATBOT_20")) {
            if (code.equals("CHATBOT_203")) { // Shop Not Found
                return HttpStatus.NOT_FOUND; // 404
            }
            return HttpStatus.SERVICE_UNAVAILABLE; // 503
        }
        
        // Request Validation Errors (301-305) -> 400 Bad Request
        if (code.startsWith("CHATBOT_30")) {
            return HttpStatus.BAD_REQUEST; // 400
        }
        
        // Redis Stream Errors (401-402) -> 503 Service Unavailable
        if (code.startsWith("CHATBOT_40")) {
            return HttpStatus.SERVICE_UNAVAILABLE; // 503
        }
        
        // General Errors (500, 999) -> 500 Internal Server Error
        return HttpStatus.INTERNAL_SERVER_ERROR; // 500
    }

    /**
     * 개발 환경 여부 확인
     */
    private boolean isDevelopmentEnvironment() {
        String profile = System.getProperty("spring.profiles.active");
        return profile != null && (profile.contains("dev") || profile.contains("local"));
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
        UserReservationErrorCode errorCode = ex.getURErrorCode();

        log.error("UserReservationException 발생: {} - {} ", errorCode, errorCode.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.of(
                errorCode.getHttpStatus().value(),
                errorCode.getCode(),
                errorCode.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(errorCode.getHttpStatus()).body(ApiResponse.fail(errorCode.getMessage(), errorResponse));
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

    // SameNameException 처리 (400 BAD_REQUEST)
    @ExceptionHandler(SameNameException.class)
    public ResponseEntity<ErrorResponse> handleSameNameException (SameNameException ex, WebRequest request) {
        log.warn("SameNameException 발생: {}", ex.getMessage());
        // ErrorResponse의 of() 팩토리 메소드 사용
        ErrorResponse errorResponse = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // SamePhoneException 처리 (400 BAD_REQUEST)
    @ExceptionHandler(SamePhoneException.class)
    public ResponseEntity<ErrorResponse> handleSamePhoneException (SamePhoneException ex, WebRequest request){
        log.warn("SamePhoneException 발생 : {}", ex.getMessage());
        ErrorResponse response = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // SamePwdException 처리(400 BAD_REQUEST)
    @ExceptionHandler(SamePwdException.class)
    public ResponseEntity<ErrorResponse> handleSamePwdException (SamePwdException ex, WebRequest request){
        //예외 발생 시 서버 로그를 남기는 코드
        log.warn("SamePwdException 발생 : {}", ex.getMessage());
        // ex.getMessage() : 발생한 예외(ex) 객체에 담긴 예외 메시지를 가져옵
        ErrorResponse response = ErrorResponse.of(
                // HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.getReasonPhrase():
                // HTTP 상태 코드 400(Bad Request)의 숫자 값(400)과
                // HTTP 상태 코드 400에 해당하는 문구인 "Bad Request"를 가져온다.
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ex.getMessage(),
                //request.getDescription(false).replace("uri=", ""): WebRequest 객체에서 요청 URI와 같은 요청 정보를 가져옵니다.
                //        getDescription(false)는 요청 URI를 uri= 형태로 반환하기 때문에
                //        replace("uri=", "")를 통해 uri= 부분을 제거하고 순수한 URI만 남긴다.
                request.getDescription(false).replace("uri=", "")
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
