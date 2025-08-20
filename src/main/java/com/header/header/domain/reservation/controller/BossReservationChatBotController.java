package com.header.header.domain.reservation.controller;

import com.header.header.domain.reservation.dto.ChatRequestDTO;
import com.header.header.domain.reservation.dto.ChatResponseDTO;
import com.header.header.domain.reservation.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/my-shops/{shopCode}/chatbot")
public class BossReservationChatBotController {

    private final ChatbotService chatbotService;

    public BossReservationChatBotController(ChatbotService chatbotService){
        this.chatbotService = chatbotService;
    }

    @PostMapping("/reservation")
    public ResponseEntity<ChatResponseDTO> sendQuestionToFastAPI(
            @PathVariable("shopCode") Integer shopCode,
            @RequestBody ChatRequestDTO request) {

        ChatResponseDTO response = chatbotService.askChatbot(shopCode, request.getQuestion());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/session")
    public String getSessionId() {
        return chatbotService.getSessionId();
    }

}
