package com.header.header.domain.message.service;

import com.header.header.domain.message.dto.MessageSendBatchDTO;
import com.header.header.domain.message.exception.InvalidBatchException;
import com.header.header.domain.message.projection.MessageBatchListView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class MessageSendBatchTests {

    @Autowired
    private MessageSendBatchService messageSendBatchService;

    private MessageSendBatchDTO testBatch;

    @BeforeEach
    void setUp(){
        MessageSendBatchDTO createDTO  = new MessageSendBatchDTO();
        createDTO.setShopCode(2);
        createDTO.setSendType("GROUP");
        createDTO.setSubject("이벤트 안내");
        createDTO.setTotalCount(10);

        testBatch  = messageSendBatchService.createMessageBatch(createDTO);
    }

    @Test
    @DisplayName("BatchList 조회")
    void readBatchLists(){
        List<MessageBatchListView> batches = messageSendBatchService.getBatchListByShop(testBatch.getShopCode());

        System.out.println("<조회된 배치 수>: " + batches.size());

        for (MessageBatchListView batch : batches) {
            System.out.println("배치 코드: " + batch.getBatchCode());
            System.out.println("제목: " + batch.getSubject());
            System.out.println("발송 날짜: " + batch.getSendDate());
            System.out.println("발송 시간: " + batch.getSendTime());
            System.out.println("발송 타입: " + batch.getSendType());
            System.out.println("------------------------");
        }

        assertNotNull(batches);
        assertFalse(batches.isEmpty());
    }
    
    @Test
    @DisplayName("Batch 상세 조회 성공")
    void readBatchDetails_Success(){
        MessageSendBatchDTO batch = messageSendBatchService.getBatchDetails(testBatch.getShopCode(),testBatch.getBatchCode());

        System.out.println(batch);

        assertNotNull(batch);
    }

    @Test
    @DisplayName("Batch 상세 조회 실패_존재하지 않는 배치 코드")
    void readBatchDetails_UnknownBatchCode_ThrowException(){
        Integer unknownBatchCode = 5;

        assertThatThrownBy(() -> messageSendBatchService.getBatchDetails(testBatch.getShopCode(),unknownBatchCode))
                .isInstanceOf(InvalidBatchException.class)
                .hasMessageContaining("존재하지 않는 배치 코드");
    }

    @Test
    @DisplayName("Batch 생성 성공")
    void createBatch_Success(){
        MessageSendBatchDTO result = messageSendBatchService.createMessageBatch(testBatch);

        System.out.println(result);

        assertNotNull(result);
    }

    @Test
    @DisplayName("Batch 업데이트 성공")
    void updateBatch_Success(){
        // update
        Integer updateSuccessCount = 6;
        Integer updateFailCount = 4;

        MessageSendBatchDTO result = messageSendBatchService.updateMessageBatchResults(testBatch.getShopCode(), testBatch.getBatchCode(),true);

        assertEquals(updateSuccessCount, result.getSuccessCount());
        assertEquals(updateFailCount, result.getFailCount());
    }

    @Test
    @DisplayName("Batch 업데이트 실패")
    void updateBatch_NonEqualResultCount_ThrowException(){
        int updateSuccessCount = 6;
        int updateFailCount = 3;

        assertThatThrownBy(() -> messageSendBatchService.updateMessageBatchResults(testBatch.getShopCode(), testBatch.getBatchCode(),false))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("일치하지 않는 결과 카운트");
    }
}
