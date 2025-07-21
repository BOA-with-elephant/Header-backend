package com.header.header.domain.message.controller;

import com.header.header.common.controller.MyShopBaseController;
import com.header.header.common.dto.response.ApiResponse;
import com.header.header.domain.message.dto.*;
import com.header.header.domain.message.service.MessageSendFacadeService;
import com.header.header.domain.message.service.MessageTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MessageController extends MyShopBaseController {

    private final MessageSendFacadeService messageSendFacadeService;
    private final MessageTemplateService messageTemplateService;

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

    @GetMapping("/template")
    public ResponseEntity<ApiResponse<List<MessageTemplateResponse>>> getTemplateList(
            @PathVariable Integer shopId) {
        List<MessageTemplateResponse> response = messageTemplateService.getAllTypeTemplateList(shopId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

}
