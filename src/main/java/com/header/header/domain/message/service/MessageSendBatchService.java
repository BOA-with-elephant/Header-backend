package com.header.header.domain.message.service;

import com.header.header.domain.message.dto.MessageSendBatchDTO;
import com.header.header.domain.message.entity.MessageSendBatch;
import com.header.header.domain.message.exception.InvalidBatchException;
import com.header.header.domain.message.projection.MessageBatchListView;
import com.header.header.domain.message.repository.MessageSendBatchRepository;
import com.header.header.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageSendBatchService {

    private final MessageTemplateService messageTemplateService;
    private final MessageSendBatchRepository messageSendBatchRepository;
    private final ModelMapper modelMapper;

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
     * 메세지 배치 상세 조회
     *
     * @param shopCode 샵 코드
     * @param batchCode 배치 코드
     * */
    public MessageSendBatchDTO getBatchDetails(Integer shopCode, Integer batchCode) {
        if (shopCode == null || batchCode == null) {
            throw new IllegalArgumentException("shopCode와 batchCode는 필수입니다.");
        }

        MessageSendBatch batch = messageSendBatchRepository.findByShopCodeAndBatchCode(shopCode, batchCode)
                .orElseThrow(() -> InvalidBatchException.invalidBatchCode("존재하지 않는 배치 코드 입니다."));

        return modelMapper.map(batch, MessageSendBatchDTO.class);
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
     * @param successCount 성공 카운트
     * @param failCount 실패 카운트
     * */
    @Transactional
    protected MessageSendBatchDTO updateMessageBatchResults(Integer shopCode, Integer batchCode,int successCount, int failCount){
        if (shopCode == null || batchCode == null) {
            throw new IllegalArgumentException("shopCode와 batchCode는 필수입니다.");
        }

        MessageSendBatch foundMessageBatch = messageSendBatchRepository.findByShopCodeAndBatchCode(shopCode, batchCode)
                .orElseThrow(() -> InvalidBatchException.invalidBatchCode("존재하지 않는 배치 코드 입니다."));


        // 결과 업데이트
        foundMessageBatch.updateBatchResultsCount(successCount, failCount);

        return modelMapper.map(foundMessageBatch, MessageSendBatchDTO.class);
    }

    /* DELETE ❌*/


    /* Authorization */
    /**
     * isAdmin=true인 user만 접근 가능
     * (이 메소드는 UserFacadeService를 통해
     * AuthUserServiceTests - adminMSBauthorize에서 사용됩니다)
     *
     * @param user
     * @throw AccessDeniedException */
    public void accessMSB(User user) {
        if (!user.isAdmin()) {
            throw new AccessDeniedException("이 페이지는 관리자만 접근 가능합니다.");
        }
    }
}
