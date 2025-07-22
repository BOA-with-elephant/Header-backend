package com.header.header.domain.message.service;

import com.header.header.domain.message.dto.MessageHistoryResponse;
import com.header.header.domain.message.dto.MessageReceiver;
import com.header.header.domain.message.dto.MessageSendBatchDTO;
import com.header.header.domain.message.entity.MessageSendBatch;
import com.header.header.domain.message.exception.InvalidBatchException;
import com.header.header.domain.message.projection.MessageBatchListView;
import com.header.header.domain.message.projection.MessageBatchResultCountView;
import com.header.header.domain.message.repository.MessageSendBatchRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageSendBatchService {

    private final MessageTemplateService messageTemplateService;
    private final MessageSendBatchRepository messageSendBatchRepository;
    private final MessageHistoryService messageHistoryService;
    private final ModelMapper modelMapper;

    /* FACADE */

    /**
     * 배치 코드를 통해 배치 성공/실패 결과 및 수신자 정보 리스트를 조회합니다.
     * @param shopCode 샵 코드
     * @param batchCode 배치 코드
     * @return MessageHistoryResponse API 응답용 DTO
     */
    public MessageHistoryResponse getMessageHistoryDetail(Integer shopCode, Integer batchCode){

        // 1. 수신자 리스트 조회
        List<MessageReceiver> receiverList = messageHistoryService.getMessageHistoryListByBatch(batchCode).stream()
                .map( receiver -> MessageReceiver.builder()
                        .name(receiver.getUserName())
                        .sentStatus(receiver.getSentStatus())
                        .sentAt(receiver.getSentAt() == null ? "-" :receiver.getSentAt().toString())
                        .etc(receiver.getSentStatus().equals("SUCCESS") ?  "" : receiver.getErrorMessage() )
                        .build()).toList();

        // 2. 배치 정보 조회.
        MessageBatchResultCountView batchDetail = getBatchDetails(shopCode, batchCode);

        // 3. 응답 생성 및 반환
        return MessageHistoryResponse.builder()
                .successCount(batchDetail.getSuccessCount())
                .failCount(batchDetail.getFailCount())
                .receivers(receiverList)
                .build();
    }


    /* READ */
    /**
     * 메세지 배치 리스트 조회
     *
     * @param shopCode 어떤 샵의 배치 리스트를 가져올지
     * @return List<MessageBatchListView>
     */
    public List<MessageBatchListView> getBatchListByShop(Integer shopCode){
        if (shopCode == null) {
            throw new IllegalArgumentException("shopCode는 필수입니다.");
        }

        // todo. 샵 검증 메서드

        return messageSendBatchRepository.findByShopCodeOrderByBatchCodeDesc(shopCode);
    }

    /**
     * 메세지 배치 상세 조회( failCount와 successCount만 조회한다. )
     *
     * @param shopCode 샵 코드
     * @param batchCode 배치 코드
     * */
    public MessageBatchResultCountView getBatchDetails(Integer shopCode, Integer batchCode) {
        if (shopCode == null || batchCode == null) {
            throw new IllegalArgumentException("shopCode와 batchCode는 필수입니다.");
        }

        MessageBatchResultCountView batch = messageSendBatchRepository.findByBatchCode(batchCode)
                .orElseThrow(() -> InvalidBatchException.invalidBatchCode("존재하지 않는 배치 코드 입니다."));

        return batch;
    }



    /* CREATE */
    /**
     * 사용자가 메세지 요청시 배치가 생성된다
     *
     * @param batchDTO batch DTO
     * */
    @Transactional
    protected MessageSendBatchDTO createMessageBatch(MessageSendBatchDTO batchDTO){
        if(batchDTO.getTemplateCode() != null){ // 존재하는 Templete 코드가 있는지 확인
            messageTemplateService.findTemplateOrThrow(batchDTO.getTemplateCode());
        }
        
        // 필수 입력값 체크
        if(batchDTO.getSendDate() == null){
            batchDTO.setSendDate(new java.sql.Date(new Date().getTime()));
        }
        if(batchDTO.getSendTime() == null){
            batchDTO.setSendTime(new java.sql.Time(new Date().getTime()));
        }

        MessageSendBatch batch = messageSendBatchRepository.save(modelMapper.map(batchDTO, MessageSendBatch.class));

        return modelMapper.map(batch, MessageSendBatchDTO.class);
    }

    /* UPDATE */
    /**
     * 사용자가 메세지 요청시 배치가 생성된다
     * INDEX 사용을 위해 샵 코드, 배치 코드를 둘 다 사용해서 조회
     * @param shopCode 샵 코드
     * @param batchCode 배치 코드
     * @param result ( true = 성공, false = 실패 )
     * */
    @Transactional
    protected MessageSendBatchDTO updateMessageBatchResults(Integer shopCode, Integer batchCode, boolean result){
        if (shopCode == null || batchCode == null) {
            throw new IllegalArgumentException("shopCode와 batchCode는 필수입니다.");
        }

        MessageSendBatch foundMessageBatch = messageSendBatchRepository.findByShopCodeAndBatchCode(shopCode, batchCode)
                .orElseThrow(() -> InvalidBatchException.invalidBatchCode("존재하지 않는 배치 코드 입니다."));

        Integer successCount = foundMessageBatch.getSuccessCount();
        Integer failCount = foundMessageBatch.getFailCount();
        // 결과 업데이트
        if(result){
            foundMessageBatch.updateBatchResultsCount( successCount+ 1, failCount);
        }
        else{
            foundMessageBatch.updateBatchResultsCount( successCount, failCount + 1);
        }

        return modelMapper.map(foundMessageBatch, MessageSendBatchDTO.class);
    }

    /* DELETE ❌*/

}
