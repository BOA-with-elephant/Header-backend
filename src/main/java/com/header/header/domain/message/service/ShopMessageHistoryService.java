package com.header.header.domain.message.service;

import com.header.header.domain.message.dto.ShopMessageHistoryDTO;
import com.header.header.domain.message.entity.ShopMessageHistory;
import com.header.header.domain.message.exception.InvalidBatchException;
import com.header.header.domain.message.projection.MessageContentView;
import com.header.header.domain.message.projection.MessageHistoryListView;
import com.header.header.domain.message.repository.ShopMessageHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopMessageHistoryService {

    private final ShopMessageHistoryRepository shopMessageHistoryRepository;
    private final ModelMapper modelMapper;

    /* Read */
    /**
     * 배치에 해당되는 메세지 히스토리 리스트 조회
     *
     * @param batchCode 어떤 배치의 수신자 목록을 가져올지
     * @return List<MessageBatchListView>
     */
    public List<MessageHistoryListView> getMessageHistoryListByBatch(Integer batchCode){
        if(batchCode == null){
            throw new IllegalArgumentException("shopCode와 batchCode는 필수입니다.");
        }

        return shopMessageHistoryRepository.findByBatchCode(batchCode);
    }

    /**
     * 수신자에게 발송한 상세 메세지 내용 조회
     * 복합 INDEX 사용을 위해 batchCode와 함께 조회합니다.
     * @param batchCode 어떤 배치의 수신자 목록을 가져올지
     * @param userCode 어떤 수신자의 메세지 내용을 가져울지
     * @return List<MessageBatchListView>
     */
    public MessageContentView getMessageContent(Integer batchCode, Integer userCode){
        if(batchCode == null || userCode == null){
            throw new IllegalArgumentException("batchCode,userCode는 필수입니다.");
        }

        return shopMessageHistoryRepository.findByBatchCodeAndUserCode(batchCode,userCode)
                .orElseThrow(() -> InvalidBatchException.invalidBatchCode("해당 수신자 정보가 없습니다.")); // todo. Exception 수정사항!!
    }

    /* Creat */
    /**
     * 수신자에게 메세지 발송 후 히스토리 생성
     *
     * @param historyDTO 메세지 내용 DTO
     * @return ShopMessageHistoryDTO
     */
    @Transactional
    public ShopMessageHistoryDTO createMessageHistory(ShopMessageHistoryDTO historyDTO){
        /* todo. 유효성 검사 */

        ShopMessageHistory result = shopMessageHistoryRepository.save(modelMapper.map(historyDTO, ShopMessageHistory.class));

        return modelMapper.map(result, ShopMessageHistoryDTO.class);
    }

    /* Update */
    /**
     * 메세지 히스토리 상태 변경(RESERVED, FAIL, SUCCESS, PENDING)
     *
     * @param historyCode 변경할 히스토리 코드
     * @param errorMessage 에러가 있을 경우 에러 메세지
     * @return ShopMessageHistoryDTO
     */
    @Transactional
    public ShopMessageHistoryDTO updateMessageStatus(Integer historyCode, String errorMessage){

        ShopMessageHistory foundMessageHistory = shopMessageHistoryRepository.findByHistoryCode(historyCode)
                .orElseThrow(() -> InvalidBatchException.invalidBatchCode("유효하지않은 히스토리 코드"));

        if(errorMessage != null){
            foundMessageHistory.markAsFailed(errorMessage);
        }else{
            foundMessageHistory.markAsSuccess();
        }

        return modelMapper.map(foundMessageHistory, ShopMessageHistoryDTO.class);
    }

}
