package com.header.header.domain.chatbot.controller;

import com.header.header.common.dto.response.ApiResponse;
import com.header.header.domain.chatbot.dto.UserReservationRequestDTO;
import com.header.header.domain.chatbot.dto.UserReservationResponseDTO;
import com.header.header.domain.chatbot.service.ChatbotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController("api/v1")
public class UserReservationChatController {

    private final ChatbotService chatbotService;

    @PostMapping("/reservation/chat")
    public ResponseEntity<ApiResponse<UserReservationResponseDTO>> sendUserReservationMessage(
            @RequestBody @Valid UserReservationRequestDTO request
    ){
        UserReservationResponseDTO response = chatbotService.sendUserReservationChat(
                request.getMessage()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
