package com.header.header.domain.chatbot.controller;

import com.header.header.auth.model.AuthDetails;
import com.header.header.common.dto.response.ApiResponse;
import com.header.header.domain.chatbot.dto.UserReservationRequestDTO;
import com.header.header.domain.chatbot.dto.UserReservationResponseDTO;
import com.header.header.domain.chatbot.service.ChatbotService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
public class UserReservationChatController {

    private final ChatbotService chatbotService;

    @PostMapping("/reservation/chat")
    public ResponseEntity<ApiResponse<UserReservationResponseDTO>> sendUserReservationMessage(
            @RequestBody @Valid UserReservationRequestDTO request,
            @AuthenticationPrincipal AuthDetails authDetails,
            HttpServletRequest httpServletRequest
    ){
        String authorizationHeader = httpServletRequest.getHeader("Authorization");

        String token = null;

        if (authorizationHeader !=null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7); // Header에서 jwt 추출
        }

        UserReservationResponseDTO response = chatbotService.sendUserReservationChat(
                token,
                request.getQuery()
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
