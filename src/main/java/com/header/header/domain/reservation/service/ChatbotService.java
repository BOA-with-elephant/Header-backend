package com.header.header.domain.reservation.service;

import com.header.header.domain.reservation.dto.ChatRequestDTO;
import com.header.header.domain.reservation.dto.ChatResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class ChatbotService {

    private final WebClient webClient;
    private String sessionId;

    public ChatbotService(WebClient.Builder builder){
        this.webClient = builder.baseUrl("http://llm:8000").build();
//        this.webClient = builder.baseUrl("http://localhost:8000").build();
    }

    public ChatResponseDTO askChatbot(Integer shopId, String question){
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
