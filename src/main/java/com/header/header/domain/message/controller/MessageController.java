package com.header.header.domain.message.controller;

import com.header.header.common.controller.MyShopBaseController;
import com.header.header.common.dto.response.ApiResponse;
import com.header.header.domain.message.dto.*;
import com.header.header.domain.message.enums.TemplateType;
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
     *
     * 최종 URL: /api/v1/my-shops/{shopId}/messages
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

        return success(messageResponse);
    }

    /**
     * 정보성 템플릿 + 광고성 템플릿 리스트를 조회합니다.
     * @param shopId 샵 코드
     * @return List<MessageTemplateResponse>
     *
     * 최종 URL: /api/v1/my-shops/{shopId}/messages/template
     */
    @GetMapping("/messages/template")
    public ResponseEntity<ApiResponse<List<MessageTemplateResponse>>> getTemplateList(
            @PathVariable Integer shopId) {
        List<MessageTemplateResponse> response = messageTemplateService.getAllTypeTemplateList(shopId);

        return success(response);
    }

    /**
     * 새로운 메세지 템플릿을 등록합니다.
     * @param shopId 샵 코드
     * @return String 등록 완료 문구
     *
     * 최종 URL: /api/v1/my-shops/{shopId}/messages/template
     */
    @PostMapping("/messages/template")
    public ResponseEntity<ApiResponse<String>> registerTemplate(
            @PathVariable Integer shopId,
            @RequestBody MessageTemplateRequest requestBody
    ) {
        MessageTemplateDTO templateDTO = MessageTemplateDTO.builder()
                .shopCode(shopId)
                .templateTitle(requestBody.getTitle())
                .templateContent(requestBody.getContent())
                .templateType(TemplateType.PROMOTIONAL)
                .build();

        messageTemplateService.createPromotionalTemplate(templateDTO);

        return success("템플릿이 정상적으로 등록되었습니다.");
    }

    /**
     * 메세지 템플릿 내용을 수정합니다.
     * @param shopId 샵 코드
     * @param requestBody 요청 바디
     * @return String 수정 완료 문구
     *
     * 최종 URL: /api/v1/my-shops/{shopId}/messages/template
     */
    @PutMapping("/messages/template")
    public ResponseEntity<ApiResponse<String>> modifyTemplateContent(
            @PathVariable Integer shopId,
            @RequestBody MessageTemplateRequest requestBody
    ){

        MessageTemplateDTO templateDTO = MessageTemplateDTO.builder()
                .templateCode(requestBody.getTemplateCode())
                .shopCode(shopId)
                .templateTitle(requestBody.getTitle())
                .templateContent(requestBody.getContent())
                .templateType(TemplateType.PROMOTIONAL)
                .build();

        messageTemplateService.modifyMessageTemplateContent(templateDTO); // 수정

        return success("템플릿이 정상적으로 수정되었습니다.");
    }

    /**
     * 메세지 템플릿을 삭제합니다.(물리적 삭제)
     * @param shopId 샵 코드
     * @param requestBody 템플릿 코드
     * @return String 삭제 완료 문구
     *
     * 최종 URL: /api/v1/my-shops/{shopId}/messages/template
     */
    @DeleteMapping("/messages/template")
    public ResponseEntity<ApiResponse<String>> deleteTemplate(
            @PathVariable Integer shopId,
            @RequestBody MessageTemplateRequest requestBody
    ){
        messageTemplateService.deleteMessageTemplate(requestBody.getTemplateCode(), shopId);

        return success("템플릿이 정상적으로 삭제되었습니다.");
    }

    /**
     * 메세지 발송 내역 리스트를 조회합니다.
     * @param shopId 샵 코드
     * @return 발송 내역 리스트
     *
     * 최종 URL: /api/v1/my-shops/{shopId}/messages/history
     */
    @GetMapping("/messages/history")
    public ResponseEntity<ApiResponse<Object>> getMessageHistoryList(
            @PathVariable Integer shopId) {
        // TODO: 구현 필요
        return success("");
    }
}