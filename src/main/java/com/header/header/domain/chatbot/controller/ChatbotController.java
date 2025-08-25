package com.header.header.domain.chatbot.controller;

import com.header.header.common.controller.MyShopBaseController;
import com.header.header.common.dto.response.ApiResponse;
import com.header.header.domain.chatbot.dto.CustomerChatRequestDTO;
import com.header.header.domain.chatbot.service.ChatbotService;
import com.header.header.domain.reservation.dto.ChatResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ChatbotController extends MyShopBaseController {

    private final ChatbotService chatbotService;

    @PostMapping("/chatbot/customer")
    public ResponseEntity<ApiResponse<ChatResponseDTO>> sendCustomerMessage(
            @PathVariable("shopId") Integer shopId,
            @RequestBody CustomerChatRequestDTO request) {

        try {
            ChatResponseDTO response = chatbotService.sendCustomerMessageAboutVisitors(
                shopId, 
                request.getMessage()
            );
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            return ResponseEntity.ok(ApiResponse.fail("고객 챗봇 요청 처리 실패: " + e.getMessage(), null));
        }
    }
}