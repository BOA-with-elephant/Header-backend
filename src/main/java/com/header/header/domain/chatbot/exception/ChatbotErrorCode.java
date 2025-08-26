package com.header.header.domain.chatbot.exception;

public enum ChatbotErrorCode {
    
    // FastAPI Communication Errors
    FASTAPI_CONNECTION_ERROR("FastAPI 서비스에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.", "CHATBOT_001"),
    FASTAPI_TIMEOUT_ERROR("응답 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.", "CHATBOT_002"),
    FASTAPI_SERVER_ERROR("AI 서비스에 일시적인 문제가 발생했습니다.", "CHATBOT_003"),
    FASTAPI_INVALID_RESPONSE("AI 서비스로부터 올바르지 않은 응답을 받았습니다.", "CHATBOT_004"),
    
    // OpenAI API Errors
    OPENAI_API_ERROR("AI 모델 서비스에 문제가 발생했습니다.", "CHATBOT_101"),
    OPENAI_QUOTA_EXCEEDED("AI 서비스 사용량이 초과되었습니다. 관리자에게 문의해주세요.", "CHATBOT_102"),
    OPENAI_RATE_LIMIT("요청이 너무 많습니다. 잠시 후 다시 시도해주세요.", "CHATBOT_103"),
    
    // Database Errors
    DATABASE_CONNECTION_ERROR("데이터베이스 연결에 문제가 발생했습니다.", "CHATBOT_201"),
    DATABASE_QUERY_ERROR("데이터 조회 중 오류가 발생했습니다.", "CHATBOT_202"),
    SHOP_NOT_FOUND("존재하지 않는 매장입니다.", "CHATBOT_203"),
    CUSTOMER_DATA_ERROR("고객 정보를 불러오는 중 오류가 발생했습니다.", "CHATBOT_204"),
    
    // Request Validation Errors
    INVALID_REQUEST_FORMAT("요청 형식이 올바르지 않습니다.", "CHATBOT_301"),
    MISSING_REQUIRED_FIELD("필수 입력 항목이 누락되었습니다.", "CHATBOT_302"),
    INVALID_SHOP_ID("올바르지 않은 매장 ID입니다.", "CHATBOT_303"),
    EMPTY_MESSAGE("메시지 내용이 비어있습니다.", "CHATBOT_304"),
    MESSAGE_TOO_LONG("메시지가 너무 깁니다. (최대 1000자)", "CHATBOT_305"),
    
    // Redis Stream Errors
    REDIS_CONNECTION_ERROR("내부 메시징 서비스 연결 오류가 발생했습니다.", "CHATBOT_401"),
    REDIS_STREAM_ERROR("메시지 처리 중 오류가 발생했습니다.", "CHATBOT_402"),
    
    // General Errors
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.", "CHATBOT_500"),
    UNKNOWN_ERROR("알 수 없는 오류가 발생했습니다.", "CHATBOT_999");

    private final String userMessage;
    private final String errorCode;

    ChatbotErrorCode(String userMessage, String errorCode) {
        this.userMessage = userMessage;
        this.errorCode = errorCode;
    }

    public String getUserMessage() {
        return userMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }
}