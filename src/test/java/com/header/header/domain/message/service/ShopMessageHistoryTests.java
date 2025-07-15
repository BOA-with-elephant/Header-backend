package com.header.header.domain.message.service;

import com.header.header.domain.message.dto.ShopMessageHistoryDTO;
import com.header.header.domain.message.enums.MessageStatus;
import com.header.header.domain.message.exception.InvalidBatchException;
import com.header.header.domain.message.projection.MessageContentView;
import com.header.header.domain.message.projection.MessageHistoryListView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ShopMessageHistoryTests {

    @Autowired
    private ShopMessageHistoryService shopMessageHistoryService;

    private ShopMessageHistoryDTO testHistory;
    private ShopMessageHistoryDTO testReservedHistory;

    @BeforeEach
    void setUp(){
        // PENDING 상태 히스토리 생성
        ShopMessageHistoryDTO createDTO = ShopMessageHistoryDTO.builder()
                .batchCode(2)
                .userCode(2)
                .msgContent("개별 메세지 내용입니다.")
                .sendStatus(String.valueOf(MessageStatus.PENDING))
                .build();

        testHistory = shopMessageHistoryService.createMessageHistory(createDTO);

        // RESERVED 상태 히스토리 생성 (추가 테스트용)
        ShopMessageHistoryDTO reservedDTO = ShopMessageHistoryDTO.builder()
                .batchCode(2)
                .userCode(3)
                .msgContent("예약 메세지 내용입니다.")
                .sendStatus(String.valueOf(MessageStatus.RESERVED))
                .build();

        testReservedHistory = shopMessageHistoryService.createMessageHistory(reservedDTO);
    }

    @Test
    @DisplayName("수신자 목록 조회 테스트")
    void getMessageHistoryListByBatch_Success() {
        // given
        Integer batchCode = testHistory.getBatchCode();

        // when
        List<MessageHistoryListView> historyList = shopMessageHistoryService.getMessageHistoryListByBatch(batchCode);

        // then
        assertNotNull(historyList);
        assertFalse(historyList.isEmpty());
        assertTrue(historyList.size() >= 2); // setUp에서 2개 생성했으므로

        // 결과 출력
        System.out.println("=== 히스토리 목록 조회 결과 ===");
        for (MessageHistoryListView history : historyList) {
            System.out.println("히스토리 코드: " + history.getHistoryCode());
            System.out.println("배치 코드: " + history.getBatchCode());
            System.out.println("사용자 코드: " + history.getUserCode());
            System.out.println("사용자 이름: " + history.getUserName());
            System.out.println("전송 상태: " + history.getSentStatus());
            System.out.println("전송 시간: " + history.getSentAt());
            System.out.println("------------------------");
        }
    }

    @Test
    @DisplayName("수신자 목록 조회 실패 - null batchCode")
    void getMessageHistoryListByBatch_NullBatchCode_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> shopMessageHistoryService.getMessageHistoryListByBatch(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("batchCode는 필수입니다");
    }

    @Test
    @DisplayName("수신자 상세 메세지 조회 테스트")
    void getMessageContent_Success() {
        // given
        Integer batchCode = testHistory.getBatchCode();
        Integer userCode = testHistory.getUserCode();

        // when
        MessageContentView messageContent = shopMessageHistoryService.getMessageContent(batchCode, userCode);

        // then
        assertNotNull(messageContent);
        assertEquals("개별 메세지 내용입니다.", messageContent.getMsgContent());

        // 결과 출력
        System.out.println("=== 메세지 내용 조회 결과 ===");
        System.out.println("메세지 내용: " + messageContent.getMsgContent());
    }

    @Test
    @DisplayName("수신자 상세 메세지 조회 실패 - 존재하지 않는 데이터")
    void getMessageContent_NotFound_ThrowsException() {
        // given
        Integer nonExistentBatchCode = 999;
        Integer nonExistentUserCode = 999;

        // when & then
        assertThatThrownBy(() -> shopMessageHistoryService.getMessageContent(nonExistentBatchCode, nonExistentUserCode))
                .isInstanceOf(InvalidBatchException.class)
                .hasMessageContaining("해당 수신자 정보가 없습니다");
    }

    @Test
    @DisplayName("수신자 상세 메세지 조회 실패 - null 파라미터")
    void getMessageContent_NullParams_ThrowsException() {
        // when & then
        assertThatThrownBy(() -> shopMessageHistoryService.getMessageContent(null, 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("batchCode,userCode는 필수입니다");

        assertThatThrownBy(() -> shopMessageHistoryService.getMessageContent(1, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("batchCode,userCode는 필수입니다");
    }

    @Test
    @DisplayName("메세지 히스토리 생성 테스트 성공")
    void createMessageHistory_Success() {
        // given
        ShopMessageHistoryDTO newHistoryDTO = ShopMessageHistoryDTO.builder()
                .batchCode(3)
                .userCode(4)
                .msgContent("새로운 메세지 내용")
                .sendStatus(String.valueOf(MessageStatus.PENDING))
                .build();

        // when
        ShopMessageHistoryDTO result = shopMessageHistoryService.createMessageHistory(newHistoryDTO);

        // then
        assertNotNull(result);
        assertNotNull(result.getHistoryCode()); // ID가 생성되었는지 확인
        assertEquals(newHistoryDTO.getBatchCode(), result.getBatchCode());
        assertEquals(newHistoryDTO.getUserCode(), result.getUserCode());
        assertEquals(newHistoryDTO.getMsgContent(), result.getMsgContent());
        assertEquals(newHistoryDTO.getSendStatus(), result.getSendStatus());

        System.out.println("=== 생성된 히스토리 ===");
        System.out.println("히스토리 코드: " + result.getHistoryCode());
        System.out.println("배치 코드: " + result.getBatchCode());
        System.out.println("사용자 코드: " + result.getUserCode());
        System.out.println("메세지 내용: " + result.getMsgContent());
        System.out.println("전송 상태: " + result.getSendStatus());
    }

    @Test
    @DisplayName("메세지 히스토리 상태 변경 PENDING -> SUCCESS")
    void updateMessageStatus_PendingToSuccess() {
        // given
        Integer historyCode = testHistory.getHistoryCode();
        String initialStatus = testHistory.getSendStatus();

        // when
        ShopMessageHistoryDTO result = shopMessageHistoryService.updateMessageStatus(historyCode, null);

        // then
        assertNotNull(result);
        assertEquals(historyCode, result.getHistoryCode());
        assertEquals(String.valueOf(MessageStatus.SUCCESS), result.getSendStatus());
        assertNotEquals(initialStatus, result.getSendStatus()); // 상태가 변경되었는지 확인
        assertNull(result.getErrorMessage()); // 성공 시 에러 메세지는 null
        assertNotNull(result.getSentAt()); // 전송 시간이 설정되었는지 확인

        System.out.println("=== 상태 변경 결과 (PENDING -> SUCCESS) ===");
        System.out.println("이전 상태: " + initialStatus);
        System.out.println("현재 상태: " + result.getSendStatus());
        System.out.println("전송 시간: " + result.getSentAt());
    }

    @Test
    @DisplayName("메세지 히스토리 상태 변경 PENDING -> FAIL")
    void updateMessageStatus_PendingToFail() {
        // given
        Integer historyCode = testHistory.getHistoryCode();
        String errorMessage = "네트워크 연결 오류";
        String initialStatus = testHistory.getSendStatus();

        // when
        ShopMessageHistoryDTO result = shopMessageHistoryService.updateMessageStatus(historyCode, errorMessage);

        // then
        assertNotNull(result);
        assertEquals(historyCode, result.getHistoryCode());
        assertEquals(String.valueOf(MessageStatus.FAIL), result.getSendStatus());
        assertNotEquals(initialStatus, result.getSendStatus()); // 상태가 변경되었는지 확인
        assertEquals(errorMessage, result.getErrorMessage()); // 에러 메세지가 설정되었는지 확인

        System.out.println("=== 상태 변경 결과 (PENDING -> FAIL) ===");
        System.out.println("이전 상태: " + initialStatus);
        System.out.println("현재 상태: " + result.getSendStatus());
        System.out.println("에러 메세지: " + result.getErrorMessage());
    }

    @Test
    @DisplayName("메세지 히스토리 상태 변경 RESERVED -> SUCCESS")
    void updateMessageStatus_ReservedToSuccess() {
        // given
        Integer historyCode = testReservedHistory.getHistoryCode();
        String initialStatus = testReservedHistory.getSendStatus();

        // when
        ShopMessageHistoryDTO result = shopMessageHistoryService.updateMessageStatus(historyCode, null);

        // then
        assertNotNull(result);
        assertEquals(historyCode, result.getHistoryCode());
        assertEquals(String.valueOf(MessageStatus.SUCCESS), result.getSendStatus());
        assertNotEquals(initialStatus, result.getSendStatus()); // 상태가 변경되었는지 확인
        assertNull(result.getErrorMessage()); // 성공 시 에러 메세지는 null
        assertNotNull(result.getSentAt()); // 전송 시간이 설정되었는지 확인

        System.out.println("=== 상태 변경 결과 (RESERVED -> SUCCESS) ===");
        System.out.println("이전 상태: " + initialStatus);
        System.out.println("현재 상태: " + result.getSendStatus());
        System.out.println("전송 시간: " + result.getSentAt());
    }

    @Test
    @DisplayName("메세지 히스토리 상태 변경 RESERVED -> FAIL")
    void updateMessageStatus_ReservedToFail() {
        // given
        Integer historyCode = testReservedHistory.getHistoryCode();
        String errorMessage = "예약 시간 초과";
        String initialStatus = testReservedHistory.getSendStatus();

        // when
        ShopMessageHistoryDTO result = shopMessageHistoryService.updateMessageStatus(historyCode, errorMessage);

        // then
        assertNotNull(result);
        assertEquals(historyCode, result.getHistoryCode());
        assertEquals(String.valueOf(MessageStatus.FAIL), result.getSendStatus());
        assertNotEquals(initialStatus, result.getSendStatus()); // 상태가 변경되었는지 확인
        assertEquals(errorMessage, result.getErrorMessage()); // 에러 메세지가 설정되었는지 확인

        System.out.println("=== 상태 변경 결과 (RESERVED -> FAIL) ===");
        System.out.println("이전 상태: " + initialStatus);
        System.out.println("현재 상태: " + result.getSendStatus());
        System.out.println("에러 메세지: " + result.getErrorMessage());
    }

    @Test
    @DisplayName("메세지 히스토리 상태 변경 실패 - 존재하지 않는 히스토리 코드")
    void updateMessageStatus_InvalidHistoryCode_ThrowsException() {
        // given
        Integer invalidHistoryCode = 999;

        // when & then
        assertThatThrownBy(() -> shopMessageHistoryService.updateMessageStatus(invalidHistoryCode, null))
                .isInstanceOf(InvalidBatchException.class)
                .hasMessageContaining("유효하지않은 히스토리 코드");
    }

    @Test
    @DisplayName("같은 배치의 여러 히스토리 조회 테스트")
    void getMessageHistoryListByBatch_MultipleSameBatch() {
        // given: setUp에서 같은 배치(2)에 2개의 히스토리가 생성됨
        Integer batchCode = 2;

        // when
        List<MessageHistoryListView> historyList = shopMessageHistoryService.getMessageHistoryListByBatch(batchCode);

        // then
        assertNotNull(historyList);
        assertTrue(historyList.size() >= 2); // 최소 2개 이상

        // 모두 같은 배치 코드인지 확인
        for (MessageHistoryListView history : historyList) {
            assertEquals(batchCode, history.getBatchCode());
        }

        System.out.println("=== 배치 " + batchCode + "의 전체 히스토리 개수: " + historyList.size() + " ===");
    }
    
}
