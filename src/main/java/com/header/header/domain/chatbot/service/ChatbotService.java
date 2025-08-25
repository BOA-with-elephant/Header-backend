package com.header.header.domain.chatbot.service;

import com.header.header.domain.reservation.dto.ChatRequestDTO;
import com.header.header.domain.reservation.dto.ChatResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

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

    public ChatResponseDTO sendCustomerMessageAboutVisitors(Integer shopId, String message) throws Exception {
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
            
            if (responseBody != null) {
                logger.info("✅ Received response from FastAPI: {}", responseBody);
                
                ChatResponseDTO chatResponse = new ChatResponseDTO();
                chatResponse.setAnswer((String) responseBody.get("answer"));
                chatResponse.setSessionId((String) responseBody.get("session_id"));
                
                return chatResponse;
            } else {
                throw new Exception("FastAPI returned null response");
            }
            
        } catch (Exception e) {
            logger.error("❌ Error calling FastAPI: {}", e.getMessage());
            throw new Exception("FastAPI call failed: " + e.getMessage());
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
}