package com.header.header.domain.message.service;

import com.header.header.domain.message.dto.MessageDTO;
import com.header.header.domain.message.dto.MessageResponse;
import com.header.header.domain.message.dto.ShopMessageHistoryDTO;
import com.header.header.domain.message.entity.ShopMessageHistory;
import com.header.header.domain.message.enums.MessageStatus;
import com.header.header.domain.message.external.coolsms.CoolSmsService;
import com.header.header.domain.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MessageAsyncService 테스트")
class MessageAsyncServiceTest {

    @Mock
    private CoolSmsService coolSmsService;

    @Mock
    private ShopMessageHistoryService shopMessageHistoryService;

    @Mock
    private UserService userService;

    @InjectMocks
    private MessageAsyncService messageAsyncService;

    private MessageDTO testRequest;
    private ShopMessageHistory mockHistoryEntity;  // Entity (Service에서 반환)
    private ShopMessageHistoryDTO mockHistoryDTO;  // DTO (테스트용)

    @BeforeEach
    void setUp() {
        // 테스트 데이터 준비
        testRequest = MessageDTO.builder()
                .to(1)  // userCode
                .from(2)
                .sendType("INDIVIDUAL")
                .subject("테스트 제목")
                .text("테스트 메시지입니다.")
                .build();

        // Mock에서 반환할 Entity 객체 (실제로는 DB에서 생성됨)
        mockHistoryEntity = new ShopMessageHistory();
        // Entity는 setter 사용 (AutoIncrement ID는 설정하지 않음)
        mockHistoryEntity.setUserCode(1);
        mockHistoryEntity.setMsgContent("테스트 메시지입니다.");
        mockHistoryEntity.setSendStatus(MessageStatus.PENDING);
        mockHistoryEntity.setSentAt(new Timestamp(System.currentTimeMillis()));

        // 테스트 검증용 DTO 객체
        mockHistoryDTO = ShopMessageHistoryDTO.builder()
                .historyCode(1001)  // 테스트용 ID
                .userCode(1)
                .msgContent("테스트 메시지입니다.")
                .sendStatus(MessageStatus.PENDING.toString())
                .sentAt(new Timestamp(System.currentTimeMillis()))
                .build();
    }

    @Test
    @DisplayName("메시지 전송 요청 시 즉시 PENDING 응답을 반환한다")
    void sendMessageAsync_ShouldReturnPendingResponse() {
        // Given - Service에서 Entity 반환
        when(shopMessageHistoryService.createMessageHistory(any(ShopMessageHistoryDTO.class)))
                .thenReturn(mockHistoryEntity);

        // When
        MessageResponse response = messageAsyncService.sendMessageAsync(testRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getResult()).isEqualTo(MessageStatus.PENDING.toString());

        // DB 저장이 호출되었는지 확인
        verify(shopMessageHistoryService).createMessageHistory(any(ShopMessageHistoryDTO.class));
    }

    @Test
    @DisplayName("PENDING 상태로 DB에 저장된다")
    void saveAsPending_ShouldSaveWithPendingStatus() {
        // Given
        when(shopMessageHistoryService.createMessageHistory(any(ShopMessageHistoryDTO.class)))
                .thenReturn(mockHistoryEntity);

        // When
        messageAsyncService.sendMessageAsync(testRequest);

        // Then - 올바른 파라미터로 저장되는지 확인 (DTO로 전달)
        verify(shopMessageHistoryService).createMessageHistory(argThat(dto ->
                dto.getUserCode().equals(1) &&
                        dto.getMsgContent().equals("테스트 메시지입니다.") &&
                        dto.getSendStatus().equals(MessageStatus.PENDING.toString())
        ));
    }

    @Test
    @DisplayName("짧은 메시지(90자 이하)는 SMS로 전송된다")
    void processMessageAsync_ShouldSendSms_WhenTextIsShort() {
        // Given
        String shortMessage = "짧은 메시지";
        ShopMessageHistory shortMsgEntity = new ShopMessageHistory();
        shortMsgEntity.setUserCode(1);
        shortMsgEntity.setMsgContent(shortMessage);
        shortMsgEntity.setSendStatus(MessageStatus.PENDING);

        when(userService.getPhoneByUserCode(1))
                .thenReturn("010-1234-5678");

        // When
        messageAsyncService.processMessageAsync(shortMsgEntity, testRequest, "TEST_001");

        // Then - SMS 전송 메서드가 호출되었는지 확인
        verify(userService, timeout(1000)).getPhoneByUserCode(1);
        verify(coolSmsService, timeout(1000)).sendSms(
                eq("010-3908-5624"),
                eq("010-1234-5678"),
                eq(shortMessage)
        );

        // LMS 메서드는 호출되지 않았는지 확인
        verify(coolSmsService, never()).sendLms(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("긴 메시지(90자 초과)는 LMS로 전송된다")
    void processMessageAsync_ShouldSendLms_WhenTextIsLong() {
        // Given
        String longMessage = "a".repeat(100);  // 100자 메시지
        ShopMessageHistory longMsgEntity = new ShopMessageHistory();
        longMsgEntity.setUserCode(1);
        longMsgEntity.setMsgContent(longMessage);
        longMsgEntity.setSendStatus(MessageStatus.PENDING);

        when(userService.getPhoneByUserCode(1))
                .thenReturn("010-1234-5678");

        // When
        messageAsyncService.processMessageAsync(longMsgEntity, testRequest, "TEST_001");

        // Then - LMS 전송 메서드가 호출되었는지 확인
        verify(userService, timeout(1000)).getPhoneByUserCode(1);
        verify(coolSmsService, timeout(1000)).sendLms(
                eq("010-3908-5624"),
                eq("010-1234-5678"),
                eq(longMessage),
                eq("알림 메시지")
        );

        // SMS 메서드는 호출되지 않았는지 확인
        verify(coolSmsService, never()).sendSms(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("전송 성공 시 SUCCESS 상태로 업데이트된다")
    void handleSuccess_ShouldUpdateStatusToSuccess() throws Exception {
        // Given - 리플렉션으로 private 메서드 테스트
        java.lang.reflect.Method method = MessageAsyncService.class
                .getDeclaredMethod("handleSuccess", ShopMessageHistory.class, String.class);
        method.setAccessible(true);

        // When
        method.invoke(messageAsyncService, mockHistoryEntity, "TEST_001");

        // Then - Entity 객체로 업데이트 호출
        verify(shopMessageHistoryService).updateMessageStatus(mockHistoryEntity.getHistoryCode(),"");
    }

    @Test
    @DisplayName("전송 실패 시 FAIL 상태로 업데이트된다")
    void handleFailure_ShouldUpdateStatusToFail() throws Exception {
        // Given
        RuntimeException testException = new RuntimeException("전송 실패 테스트");

        java.lang.reflect.Method method = MessageAsyncService.class
                .getDeclaredMethod("handleFailure", ShopMessageHistory.class, Exception.class, String.class);
        method.setAccessible(true);

        // When
        method.invoke(messageAsyncService, mockHistoryEntity, testException, "TEST_001");

        // Then - Entity 객체로 업데이트 호출
        verify(shopMessageHistoryService).updateMessageStatus(mockHistoryEntity.getHistoryCode(),testException.getMessage());
    }

    @Test
    @DisplayName("전화번호 조회 실패 시 에러 메시지가 저장된다")
    void processMessageAsync_ShouldSaveErrorMessage_WhenPhoneNumberNotFound() {
        // Given
        String expectedErrorMessage = "사용자를 찾을 수 없습니다";

        when(userService.getPhoneByUserCode(1))
                .thenThrow(new RuntimeException(expectedErrorMessage));

        // When
        messageAsyncService.processMessageAsync(mockHistoryEntity, testRequest, "TEST_001");

        // Then - 전화번호 조회 실패 에러 메시지 확인
        verify(userService, timeout(1000)).getPhoneByUserCode(1);
        verify(shopMessageHistoryService, timeout(1000))
                .updateMessageStatus(
                        eq(mockHistoryEntity.getHistoryCode()),
                        eq(expectedErrorMessage)  // ← 전화번호 조회 실패 메시지
                );
    }

    @Test
    @DisplayName("CoolSMS 전송 실패 시 예외가 처리된다")
    void processMessageAsync_ShouldHandleException_WhenCoolSmsThrowsException() {
        // Given
        String expectedErrorMessage = "CoolSMS API 오류";  // ← 예상 에러 메시지

        when(userService.getPhoneByUserCode(1))
                .thenReturn("010-1234-5678");

        doThrow(new RuntimeException(expectedErrorMessage))  // ← 특정 에러 메시지로 예외 발생
                .when(coolSmsService).sendSms(anyString(), anyString(), anyString());

        // When
        messageAsyncService.processMessageAsync(mockHistoryEntity, testRequest, "TEST_001");

        // Then - 에러 메시지까지 검증
        verify(userService, timeout(1000)).getPhoneByUserCode(1);
        verify(coolSmsService, timeout(1000)).sendSms(anyString(), anyString(), anyString());

        // updateMessageStatus에 에러 메시지가 전달되는지 확인
        verify(shopMessageHistoryService, timeout(1000))
                .updateMessageStatus(
                        eq(mockHistoryEntity.getHistoryCode()),
                        eq(expectedErrorMessage)  // ← 에러 메시지 검증
                );
    }

    @Test
    @DisplayName("saveAsPending 메서드가 올바른 History 객체를 반환한다")
    void saveAsPending_ShouldReturnCorrectHistory() throws Exception {
        // Given
        when(shopMessageHistoryService.createMessageHistory(any(ShopMessageHistoryDTO.class)))
                .thenReturn(mockHistoryEntity);

        // When - 리플렉션으로 private 메서드 테스트
        java.lang.reflect.Method method = MessageAsyncService.class
                .getDeclaredMethod("saveAsPending", MessageDTO.class);
        method.setAccessible(true);
        ShopMessageHistory result = (ShopMessageHistory) method.invoke(messageAsyncService, testRequest);

        // Then - Entity 객체 검증
        assertThat(result).isNotNull();
        assertThat(result.getUserCode()).isEqualTo(1);
        assertThat(result.getMsgContent()).isEqualTo("테스트 메시지입니다.");
        assertThat(result.getSendStatus()).isEqualTo(MessageStatus.PENDING);
    }

    @Test
    @DisplayName("TaskId가 8자리로 생성된다")
    void generateTaskId_ShouldReturn8Characters() throws Exception {
        // Given - 리플렉션으로 private 메서드 접근
        java.lang.reflect.Method method = MessageAsyncService.class
                .getDeclaredMethod("generateTaskId");
        method.setAccessible(true);

        // When
        String taskId = (String) method.invoke(messageAsyncService);

        // Then
        assertThat(taskId).isNotNull();
        assertThat(taskId).hasSize(8);
        assertThat(taskId).matches("[a-f0-9]{8}");  // UUID 형식 확인
    }
}