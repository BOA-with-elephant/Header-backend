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

@RestController
@RequiredArgsConstructor
public class MessageController extends MyShopBaseController {

    private final MessageSendFacadeService messageSendFacadeService;
    private final MessageTemplateService messageTemplateService;

    /**
     * 메세지를 발송합니다.(즉시발송 or 예약발송)
     * @param shopId 샵 코드
     * @param requestBody 요청 본문
     * @return MessageResponse
     */
    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @PathVariable Integer shopId,
            @RequestBody MessageRequest requestBody)
    {
        MessageResponse messageResponse  = null;

        if(!requestBody.getIsScheduled()){
            messageResponse =  messageSendFacadeService.sendImmediateMessage(requestBody);
        }// todo. 예약 발송일 경우. else ~

        return ResponseEntity.ok(ApiResponse.success(messageResponse));
    }

    /**
     * 정보성 템플릿 + 광고성 템플릿 리스트를 조회합니다.
     * @param shopId 샵 코드
     * @return List<MessageTemplateResponse>
     */
    @GetMapping("/template")
    public ResponseEntity<ApiResponse<List<MessageTemplateResponse>>> getTemplateList(
            @PathVariable Integer shopId) {
        List<MessageTemplateResponse> response = messageTemplateService.getAllTypeTemplateList(shopId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 새로운 메세지 템플릿을 등록합니다.
     * @param shopId 샵 코드
     * @return String 등록 완료 문구
     */
    @PostMapping("/template")
    public ResponseEntity<ApiResponse<List<MessageTemplateResponse>>> registerTemplate(
            @PathVariable Integer shopId) {

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 메세지 템플릿 내용을 수정합니다.
     * @param shopId 샵 코드
     * @param templateCode 수정할 템플릿 코드
     * @return String 수정 완료 문구
     */
    @PutMapping("/template/{templateCode}")
    public ResponseEntity<ApiResponse<String>> modifyTemplateContent(
            @PathVariable Integer shopId,
            @PathVariable Integer templateCode
    ){

        return ResponseEntity.ok(ApiResponse.success(null));
    }

    /**
     * 메세지 템플릿을 삭제합니다.(물리적 삭제)
     * @param shopId 샵 코드
     * @param templateCode 템플릿 코드
     * @return String 삭제 완료 문구
     */
    @DeleteMapping("/template/{templateCode}")
    public ResponseEntity<ApiResponse<String>> deleteTemplate(
            @PathVariable Integer shopId,
            @PathVariable Integer templateCode
    ){
        return ResponseEntity.ok(ApiResponse.success(null));
    }



}
