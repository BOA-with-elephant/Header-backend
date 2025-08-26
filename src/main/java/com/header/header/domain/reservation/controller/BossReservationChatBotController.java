package com.header.header.domain.reservation.controller;

import com.header.header.domain.reservation.dto.ChatRequestDTO;
import com.header.header.domain.reservation.dto.ChatResponseDTO;
import com.header.header.domain.reservation.service.BossReservationChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/my-shops/{shopCode}/chatbot")
public class BossReservationChatBotController {

    private final BossReservationChatbotService bossReservationChatbotService;

    public BossReservationChatBotController(BossReservationChatbotService bossReservationChatbotService){
        this.bossReservationChatbotService = bossReservationChatbotService;
    }

    @PostMapping("/reservation")
    public ResponseEntity<ChatResponseDTO> sendQuestionToFastAPI(
            @PathVariable("shopCode") Integer shopCode,
            @RequestBody ChatRequestDTO request) {

        ChatResponseDTO response = bossReservationChatbotService.askChatbot(shopCode, request.getQuestion());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/session")
    public String getSessionId() {
        return bossReservationChatbotService.getSessionId();
    }

}
