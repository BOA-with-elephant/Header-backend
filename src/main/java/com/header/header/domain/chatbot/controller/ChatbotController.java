package com.header.header.domain.chatbot.controller;

import com.header.header.common.controller.MyShopBaseController;
import com.header.header.common.dto.response.ApiResponse;
import com.header.header.domain.chatbot.dto.CustomerChatRequestDTO;
import com.header.header.domain.chatbot.service.ChatbotService;
import com.header.header.domain.reservation.dto.ChatResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

@RestController
@RequiredArgsConstructor
public class ChatbotController extends MyShopBaseController {

    private final ChatbotService chatbotService;

    @PostMapping("/chatbot/customer")
    public ResponseEntity<ApiResponse<ChatResponseDTO>> sendCustomerMessage(
            @PathVariable("shopId") @Positive(message = "올바르지 않은 매장 ID입니다.") Integer shopId,
            @RequestBody @Valid CustomerChatRequestDTO request) {

        ChatResponseDTO response = chatbotService.sendCustomerMessageAboutVisitors(
            shopId, 
            request.getMessage()
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}