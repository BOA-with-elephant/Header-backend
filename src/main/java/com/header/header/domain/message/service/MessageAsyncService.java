package com.header.header.domain.message.service;

import com.header.header.domain.message.dto.MessageRequest;
import com.header.header.domain.message.dto.MessageResponse;
import com.header.header.domain.message.dto.MessageSendBatchDTO;
import com.header.header.domain.message.dto.ShopMessageHistoryDTO;
import com.header.header.domain.message.entity.ShopMessageHistory;
import com.header.header.domain.message.enums.MessageStatus;
import com.header.header.domain.message.external.coolsms.CoolSmsService;
import com.header.header.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageAsyncService {

    private final CoolSmsService coolSmsService;
    private final ShopMessageHistoryService shopMessageHistoryService;
    private final MessageSendBatchService messageSendBatchService;
    private final UserService userService;

    /**
     * 즉시 응답 + 백그라운드 처리\
     */
    public MessageResponse sendMessageAsync(MessageRequest request){

        String taskId = generateTaskId();
        log.info("📩 [{}] 메시지 요청 접수 - UserCode: {}", taskId, request.getTo());

        // 1. DB에 PENDING으로 저장
        ShopMessageHistory history = saveAsPending(request);

        // 2. PENDING으로 응답
        MessageResponse response = new MessageResponse(MessageStatus.PENDING.toString());

        // 3. 백그라운드에서 비동기 처리
        processMessageAsync(history, request, taskId);
        log.info("⚡ [{}] PENDING 응답 즉시 반환", taskId);

        return response;
    }

    private ShopMessageHistory saveAsPending(MessageRequest request){
        // batch 저장
        MessageSendBatchDTO batchDTO = MessageSendBatchDTO.builder()
                .shopCode(request.getFrom())
                .templateCode(request.getTemplateCode())
                .sendType(request.getSendType())
                .subject(request.getSubject())
                .build();

        MessageSendBatchDTO createdBatchDTO = messageSendBatchService.createMessageBatch(batchDTO);

        // history 저장
        ShopMessageHistoryDTO historyDTO = ShopMessageHistoryDTO.builder()
                .batchCode(createdBatchDTO.getBatchCode())
                .userCode(request.getTo())
                .msgContent(request.getText())
                .sendStatus(MessageStatus.PENDING.toString())
                .build();

        return shopMessageHistoryService.createMessageHistory(historyDTO);
    }

    /**
     * 백그라운드에서 실제 SMS 전송 (@Async로 비동기 처리)
     */
    @Async("smsExecutor")
    public void processMessageAsync(ShopMessageHistory history,
                                    MessageRequest request,
                                    String taskId) {

        log.info("🔄 [{}] 백그라운드 처리 시작 - Thread: {}",
                taskId, Thread.currentThread().getName());

        try {
            // 1. 전화번호 조회
            String phoneNumber = userService.getPhoneByUserCode(history.getUserCode());
            log.info("📞 [{}] 전화번호 조회 완료: {}", taskId, phoneNumber);

            // 2. 메시지 타입 결정 (SMS vs LMS)
            boolean isLms = history.getMsgContent().length() > 90;

            // 3. CoolSMS SDK로 실제 전송
            if (isLms) {
                coolSmsService.sendLms(
                        "010-3908-5624",  // 발신번호 (테스트용)
                        phoneNumber,
                        history.getMsgContent(),
                        "알림 메시지"      // 제목 (테스트용)
                );
            } else {
                coolSmsService.sendSms(
                        "010-3908-5624",  // 발신번호 (테스트용)
                        phoneNumber,
                        history.getMsgContent()
                );
            }

            // 4. 성공 처리
            handleSuccess(history, taskId);

        } catch (Exception e) {
            // 5. 실패 처리
            handleFailure(history, e, taskId);
        }
    }
    private void handleSuccess(ShopMessageHistory history, String taskId) {
        try {
            log.info("✅ [{}] SMS 전송 성공", taskId);

            history.updateStatus(MessageStatus.SUCCESS, "전송 완료");
            shopMessageHistoryService.updateMessageStatus(history.getHistoryCode(),"");

        } catch (Exception e) {
            log.error("[{}] 성공 처리 중 오류", taskId, e);
        }
    }

    private void handleFailure(ShopMessageHistory history, Exception error, String taskId) {
        try {
            log.error("❌ [{}] SMS 전송 실패 - Error: {}", taskId, error.getMessage());

            history.updateStatus(MessageStatus.FAIL, error.getMessage());
            shopMessageHistoryService.updateMessageStatus(history.getHistoryCode(),error.getMessage());

        } catch (Exception e) {
            log.error("[{}] 실패 처리 중 오류", taskId, e);
        }
    }

    private String generateTaskId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

}
