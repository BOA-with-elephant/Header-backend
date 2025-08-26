package com.header.header.domain.chatbot.exception;

import lombok.Getter;

@Getter
public class ChatbotException extends RuntimeException {
    
    private final ChatbotErrorCode errorCode;
    private final String developerMessage;

    public ChatbotException(ChatbotErrorCode errorCode, String developerMessage) {
        super(errorCode.getUserMessage());
        this.errorCode = errorCode;
        this.developerMessage = developerMessage;
    }

    public ChatbotException(ChatbotErrorCode errorCode, String developerMessage, Throwable cause) {
        super(errorCode.getUserMessage(), cause);
        this.errorCode = errorCode;
        this.developerMessage = developerMessage;
    }

    public ChatbotErrorCode getErrorCode() {
        return errorCode;
    }

    public String getDeveloperMessage() {
        return developerMessage;
    }

    public String getUserMessage() {
        return errorCode.getUserMessage();
    }
}