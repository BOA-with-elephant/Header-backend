package com.header.header.domain.chatbot.service;

import com.header.header.domain.chatbot.exception.ChatbotException;
import com.header.header.domain.chatbot.exception.ChatbotErrorCode;
import com.header.header.domain.reservation.dto.ChatRequestDTO;
import com.header.header.domain.reservation.dto.ChatResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.net.ConnectException;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Service
public class ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);
    
    private final WebClient webClient;

    private String sessionId;

    public ChatbotService(WebClient.Builder webClientBuilder) {
        // Auto-detect environment: Docker (llm) or Local (localhost)
        String baseUrl = detectFastApiUrl();
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        logger.info("🌐 FastAPI WebClient initialized with URL: {}", baseUrl);
    }
    
    private String detectFastApiUrl() {
        // Check if we're in Docker environment by looking for container hostname
        String hostname = System.getenv("HOSTNAME");
        if (hostname != null && hostname.contains("header-backend")) {
            logger.info("🐳 Docker environment detected, using container name");
            return "http://llm:8000";
        }
        
        // Check if llm service is available (Docker compose)
        try {
            java.net.InetAddress.getByName("llm");
            logger.info("🐳 Docker service 'llm' found");
            return "http://llm:8000";
        } catch (java.net.UnknownHostException e) {
            logger.info("💻 Local development environment detected");
            return "http://localhost:8000";
        }
    }

    public ChatResponseDTO sendCustomerMessageAboutVisitors(Integer shopId, String message) {
        logger.info("📤 Sending message to FastAPI: Shop={}, Message={}", shopId, message);

        // Create form data (matching your test script)
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("question", message);
        formData.add("shop_id", shopId.toString());
        
        try {
            Map<String, Object> responseBody = webClient.post()
                    .uri("/api/v1/visitors/ask_with_shop")
                    .body(BodyInserters.fromFormData(formData))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            if (responseBody == null) {
                throw new ChatbotException(
                    ChatbotErrorCode.FASTAPI_INVALID_RESPONSE,
                    "FastAPI returned null response for shop: " + shopId
                );
            }
            
            // Validate response structure
            validateResponse(responseBody);
            
            logger.info("✅ Received response from FastAPI: {}", responseBody);
            
            ChatResponseDTO chatResponse = new ChatResponseDTO();
            chatResponse.setAnswer((String) responseBody.get("answer"));
            chatResponse.setSessionId((String) responseBody.get("session_id"));
            
            return chatResponse;
            
        } catch (WebClientRequestException e) {
            // Connection errors (FastAPI is down)
            throw new ChatbotException(
                ChatbotErrorCode.FASTAPI_CONNECTION_ERROR,
                "Failed to connect to FastAPI: " + e.getMessage(),
                e
            );
        } catch (WebClientResponseException e) {
            // HTTP error responses from FastAPI
            handleFastApiHttpErrors(e);
            throw new ChatbotException(
                ChatbotErrorCode.FASTAPI_SERVER_ERROR,
                "FastAPI returned HTTP " + e.getStatusCode() + ": " + e.getResponseBodyAsString(),
                e
            );
        } catch (WebClientException e) {
            // Other WebClient errors (timeouts, etc.)
            if (e.getCause() instanceof TimeoutException) {
                throw new ChatbotException(
                    ChatbotErrorCode.FASTAPI_TIMEOUT_ERROR,
                    "FastAPI request timeout for shop: " + shopId,
                    e
                );
            }
            throw new ChatbotException(
                ChatbotErrorCode.FASTAPI_CONNECTION_ERROR,
                "WebClient error: " + e.getMessage(),
                e
            );
        } catch (ChatbotException e) {
            // Re-throw our custom exceptions
            throw e;
        } catch (Exception e) {
            // Catch any other unexpected exceptions
            throw new ChatbotException(
                ChatbotErrorCode.INTERNAL_SERVER_ERROR,
                "Unexpected error during FastAPI call: " + e.getMessage(),
                e
            );
        }
    }

    public ChatResponseDTO sendCustomerMessageAboutBossReservation(Integer shopId, String question){
        ChatRequestDTO request = new ChatRequestDTO(question);

        ChatResponseDTO response = webClient.post()
                .uri("/api/v1/my-shops/{shopId}/chatbot/reservation", shopId)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatResponseDTO.class)
                .block();

        // FastAPI가 내려준 sessionId 저장
        if (response != null && response.getSessionId() != null) {
            this.sessionId = response.getSessionId();
        }

        return response;
    }

    public String getSessionId(){
        return sessionId;
    }
    
    /**
     * Validate FastAPI response structure
     */
    private void validateResponse(Map<String, Object> responseBody) {
        if (!responseBody.containsKey("answer")) {
            throw new ChatbotException(
                ChatbotErrorCode.FASTAPI_INVALID_RESPONSE,
                "Missing 'answer' field in FastAPI response: " + responseBody
            );
        }
        
        Object answer = responseBody.get("answer");
        if (answer == null || !StringUtils.hasText(answer.toString())) {
            throw new ChatbotException(
                ChatbotErrorCode.FASTAPI_INVALID_RESPONSE,
                "Empty 'answer' field in FastAPI response: " + responseBody
            );
        }
    }
    
    /**
     * Handle specific FastAPI HTTP error responses
     */
    private void handleFastApiHttpErrors(WebClientResponseException e) {
        String responseBody = e.getResponseBodyAsString();
        
        // Check for OpenAI API specific errors in response
        if (responseBody.contains("quota_exceeded") || responseBody.contains("insufficient_quota")) {
            throw new ChatbotException(
                ChatbotErrorCode.OPENAI_QUOTA_EXCEEDED,
                "OpenAI quota exceeded. Response: " + responseBody,
                e
            );
        }
        
        if (responseBody.contains("rate_limit") || e.getStatusCode().value() == 429) {
            throw new ChatbotException(
                ChatbotErrorCode.OPENAI_RATE_LIMIT,
                "OpenAI rate limit exceeded. Response: " + responseBody,
                e
            );
        }
        
        if (responseBody.contains("openai") || responseBody.contains("OpenAI")) {
            throw new ChatbotException(
                ChatbotErrorCode.OPENAI_API_ERROR,
                "OpenAI API error. Response: " + responseBody,
                e
            );
        }
        
        // Database errors from FastAPI
        if (responseBody.contains("database") || responseBody.contains("Database") || 
            responseBody.contains("mysql") || responseBody.contains("connection")) {
            throw new ChatbotException(
                ChatbotErrorCode.DATABASE_CONNECTION_ERROR,
                "Database error from FastAPI. Response: " + responseBody,
                e
            );
        }
    }
}