package com.header.header.domain.message.service;

import com.header.header.domain.message.dto.MessageDTO;
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

import java.util.ArrayList;
import java.util.List;
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
     * 즉시 응답 + 백그라운드 처리
     */
    public MessageResponse sendMessageAsync(MessageDTO request){

        String taskId = generateTaskId();
        log.info("📩 [{}] 메시지 요청 접수 - UserCode 수: {}", taskId, request.getTo().size());

        // 1. batch DB에 저장
        MessageSendBatchDTO batchDTO = MessageSendBatchDTO.builder()
                .shopCode(request.getFrom())
                .sendType(request.getSendType())
                .subject(request.getSubject())
                .totalCount(request.getTo().size())
                .successCount(0)
                .failCount(0)
                .build();

        MessageSendBatchDTO createdBatchDTO = messageSendBatchService.createMessageBatch(batchDTO);

        // 2. history 저장 : 다수의 인원일 경우 그 수만큼 저장한다.
        List<ShopMessageHistory> historyList = new ArrayList<>();

        for(Integer userCode : request.getTo()){
            historyList.add(saveAsPending(userCode, request.getText(), createdBatchDTO));
        }

        // 3. PENDING으로 응답
        MessageResponse response = new MessageResponse(MessageStatus.PENDING.toString());

        // 4. ✅ 단일 비동기 작업으로 모든 메시지 처리 (배치 처리)
        processAllMessagesAsync(historyList, request, taskId, createdBatchDTO.getBatchCode());

        log.info("⚡ [{}] PENDING 응답 즉시 반환", taskId);
        return response;
    }

    private ShopMessageHistory saveAsPending(Integer userCode, String contents, MessageSendBatchDTO batchDTO){

        ShopMessageHistoryDTO historyDTO = ShopMessageHistoryDTO.builder()
                .batchCode(batchDTO.getBatchCode())
                .userCode(userCode)
                .msgContent(contents)
                .sendStatus(MessageStatus.PENDING.toString())
                .build();

        return shopMessageHistoryService.createMessageHistory(historyDTO);
    }

    /**
     * 배치로 모든 메시지 처리 (단일 스레드에서 순차 처리)
     */
    @Async("messageTaskExecutor")
    public void processAllMessagesAsync(List<ShopMessageHistory> historyList,
                                        MessageDTO request,
                                        String taskId,
                                        Integer batchCode){
        log.info("📦 [{}] 전체 메시지 처리 시작 - 총 {}건", taskId, historyList.size());

        int successCount = 0;
        int failCount = 0;

        for(ShopMessageHistory history : historyList) {
            try {
                // SMS 전송 시도
                sendSingleMessage(history, request, taskId);

                // 성공 처리
                successCount++;
                updateMessageStatus(history, taskId, MessageStatus.SUCCESS, null);

                // API 제한 방지를 위한 딜레이
                if((successCount + failCount) % 10 == 0) {
                    Thread.sleep(200); // 10건마다 0.2초 대기
                }

            } catch (Exception e) {
                // 실패 처리
                failCount++;
                log.error("[{}] 메시지 전송 실패 - UserCode: {}, Error: {}",
                        taskId, history.getUserCode(), e.getMessage());
                updateMessageStatus(history, taskId, MessageStatus.FAIL, e);
            }
        }

        // 배치 결과 업데이트
        updateBatchResult(request.getFrom(), batchCode, successCount, failCount, taskId);

        log.info("✅ [{}] 전체 처리 완료 - 성공: {}건, 실패: {}건", taskId, successCount, failCount);
    }

    /**
     * 단일 메시지 전송 (Exception 발생 시 throws)
     */
    private void sendSingleMessage(ShopMessageHistory history,
                                   MessageDTO request,
                                   String taskId) throws Exception {

        log.debug("🔄 [{}] 메시지 처리 시작 - UserCode: {}, Thread: {}",
                taskId, history.getUserCode(), Thread.currentThread().getName());

        // 1. 전화번호 조회
        String phoneNumber = userService.getPhoneByUserCode(history.getUserCode());
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            throw new RuntimeException("전화번호를 찾을 수 없습니다: UserCode=" + history.getUserCode());
        }

        log.debug("📞 [{}] 전화번호 조회 완료: {}", taskId, phoneNumber);

        // 2. 메시지 타입 결정 (SMS vs LMS)
        boolean isLms = history.getMsgContent().length() > 90;

        // 3. CoolSMS SDK로 실제 전송
        try {
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

            log.debug("📤 [{}] SMS 전송 API 호출 완료 - UserCode: {}", taskId, history.getUserCode());

        } catch (Exception e) {
            throw new RuntimeException("SMS 전송 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 메시지 상태 업데이트
     */
    private void updateMessageStatus(ShopMessageHistory history, String taskId, MessageStatus status, Exception error){
        if(MessageStatus.SUCCESS == status){
            handleSuccess(history, taskId);
        } else {
            handleFailure(history, taskId, error);
        }
    }

    /**
     * 성공 처리
     */
    private void handleSuccess(ShopMessageHistory history, String taskId) {
        try {
            log.debug("✅ [{}] SMS 전송 성공 - UserCode: {}", taskId, history.getUserCode());
            shopMessageHistoryService.updateMessageStatus(history.getHistoryCode(), null);

        } catch (Exception e) {
            log.error("[{}] 성공 처리 중 오류 - UserCode: {}", taskId, history.getUserCode(), e);
        }
    }

    /**
     * 실패 처리
     */
    private void handleFailure(ShopMessageHistory history, String taskId, Exception error) {
        try {
            String errorMessage = error != null ? error.getMessage() : "알 수 없는 오류";
            log.error("❌ [{}] SMS 전송 실패 - UserCode: {}, Error: {}",
                    taskId, history.getUserCode(), errorMessage);

            shopMessageHistoryService.updateMessageStatus(history.getHistoryCode(), errorMessage);

        } catch (Exception e) {
            log.error("[{}] 실패 처리 중 오류 - UserCode: {}", taskId, history.getUserCode(), e);
        }
    }

    /**
     * 배치 결과 업데이트
     */
    private void updateBatchResult(Integer shopCode, Integer batchCode, int successCount, int failCount, String taskId) {
        try {
            // 성공한 건수만큼 업데이트
            for(int i = 0; i < successCount; i++) {
                messageSendBatchService.updateMessageBatchResults(shopCode, batchCode, true);
            }

            // 실패한 건수만큼 업데이트
            for(int i = 0; i < failCount; i++) {
                messageSendBatchService.updateMessageBatchResults(shopCode, batchCode, false);
            }

            log.info("📊 [{}] 배치 결과 업데이트 완료 - 성공: {}, 실패: {}", taskId, successCount, failCount);

        } catch (Exception e) {
            log.error("[{}] 배치 결과 업데이트 중 오류", taskId, e);
        }
    }

    /**
     * 작업 ID 생성
     */
    private String generateTaskId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}