package com.header.header.domain.message.controller;

import com.header.header.common.controller.MyShopBaseController;
import com.header.header.common.dto.response.ApiResponse;
import com.header.header.domain.message.dto.MessageRequest;
import com.header.header.domain.message.dto.MessageResponse;
import com.header.header.domain.message.service.MessageSendFacadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MessageController extends MyShopBaseController {

    private final MessageSendFacadeService messageSendFacadeService;

    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable Integer shopId,
            @RequestBody MessageRequest requestBody)
    {
        MessageResponse messageResponse  = null;

        if(!requestBody.getIsScheduled()){
            messageResponse =  messageSendFacadeService.sendImmediateMessage(requestBody);
        }// todo. 예약 발송일 경우.

        return ResponseEntity.ok(ApiResponse.success(messageResponse));
    }
}
