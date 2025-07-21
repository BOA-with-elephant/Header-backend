package com.header.header.domain.message.service;

import com.header.header.domain.message.dto.MessageDTO;
import com.header.header.domain.message.dto.MessageResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;

@SpringBootTest
@ActiveProfiles("test")
@Slf4j
@DisplayName("메시지 통합 테스트")
class MessageIntegrationTest {

    @Autowired
    private MessageAsyncService messageAsyncService;

    @Test
    @Disabled("수동 실행 - 실제 SMS 전송됨! 💸")
    @DisplayName("실제 SMS 전송 테스트")
    void realSmsTest() throws InterruptedException {

        log.info("🚀 실제 SMS 전송 테스트 시작!");

        // Given - 실제 전송할 메시지
        MessageDTO request = MessageDTO.builder()
                .to(list(1))  // 본인 userCode (DB에 실제 존재하는 값)
                .from(1)  // shopCode  
                .sendType("INDIVIDUAL")
                .subject("개발 테스트")
                .text("안녕하세요! 실제 SMS 통합 테스트입니다 😊 " + System.currentTimeMillis())
                .build();

        log.info("📱 전송할 메시지: {}", request.getText());

        // When - 실제 전송
        MessageResponse response = messageAsyncService.sendMessageAsync(request);

        // Then - 즉시 응답 확인
        assertThat(response).isNotNull();
        assertThat(response.getResult()).isEqualTo("PENDING");

        log.info("⚡ PENDING 응답 확인 완료: {}", response.getResult());
        log.info("🔄 백그라운드에서 실제 전송 중... 잠시만 기다려주세요");

        // 백그라운드 처리 대기 (실제 전송 완료까지)
        Thread.sleep(10000);  // 10초 대기

        log.info("✅ 통합 테스트 완료! 휴대폰 확인해보세요 📱");
        log.info("💡 DB에서 전송 상태도 확인해보세요 (PENDING → SUCCESS)");
    }



    @Test
    //@Disabled("수동 실행 - 실제 단체 SMS 전송됨! 💸💸💸")
    @DisplayName("단체 SMS 전송 테스트 (배치 처리)")
    void bulkSmsTest() throws InterruptedException {

        log.info("🚀 단체 SMS 전송 테스트 시작!");

        // Given - 여러 수신자에게 보낼 메시지
        MessageDTO request = MessageDTO.builder()
                .to(list(1, 2, 3))  // 여러 userCode (DB에 실제 존재하는 값들로 변경 필요)
                .from(1)  // shopCode
                .sendType("GROUP")
                .subject("단체 발송 테스트")
                .text("🎉 단체 SMS 테스트입니다! 배치 처리로 전송 중... " + System.currentTimeMillis())
                .build();

        log.info("📱 수신자 수: {}명", request.getTo().size());
        log.info("📱 전송할 메시지: {}", request.getText());

        // When - 단체 전송
        long startTime = System.currentTimeMillis();
        MessageResponse response = messageAsyncService.sendMessageAsync(request);
        long responseTime = System.currentTimeMillis() - startTime;

        // Then - 즉시 응답 확인
        assertThat(response).isNotNull();
        assertThat(response.getResult()).isEqualTo("PENDING");

        log.info("⚡ PENDING 응답 시간: {}ms", responseTime);
        log.info("⚡ PENDING 응답 확인 완료: {}", response.getResult());
        log.info("🔄 백그라운드에서 {}명에게 배치 전송 중...", request.getTo().size());

        // 배치 처리 완료 대기 (수신자 수 * 0.5초 + 여유시간)
        int waitTime = request.getTo().size() * 500 + 5000;  // 수신자 수에 따라 대기시간 조정
        log.info("⏰ {}초 대기 중... (수신자 {}명 처리 예상시간)", waitTime / 1000, request.getTo().size());

        Thread.sleep(waitTime);

        log.info("✅ 단체 SMS 테스트 완료!");
        log.info("📱 모든 수신자의 휴대폰을 확인해보세요!");
        log.info("💡 DB에서 배치 상태도 확인해보세요:");
        log.info("   - message_send_batch 테이블: total_count, success_count, fail_count");
        log.info("   - shop_message_history 테이블: 각 수신자별 send_status (PENDING → SUCCESS/FAIL)");
        log.info("🎯 배치 처리 성능 확인: 단일 스레드로 순차 처리됨");
    }

    @Test
    @Disabled("수동 실행 - 긴 메시지 LMS 테스트")
    @DisplayName("실제 LMS 전송 테스트")
    void realLmsTest() throws InterruptedException {

        log.info("🚀 실제 LMS 전송 테스트 시작!");

        // Given - 긴 메시지 (LMS로 전송됨)
        String longMessage = "안녕하세요! 이것은 LMS 테스트 메시지입니다. " +
                "SMS는 90자를 초과하면 자동으로 LMS로 전송됩니다. " +
                "현재 시간: " + System.currentTimeMillis() + " " +
                "테스트용 긴 메시지입니다. 여러분의 휴대폰으로 잘 도착했나요?";

        MessageDTO request = MessageDTO.builder()
                .to(list(1))
                .from(1)
                .sendType("INDIVIDUAL")
                .subject("LMS 테스트")
                .text(longMessage)
                .build();

        log.info("📱 LMS 메시지 길이: {}자", longMessage.length());

        // When
        MessageResponse response = messageAsyncService.sendMessageAsync(request);

        // Then
        assertThat(response.getResult()).isEqualTo("PENDING");

        log.info("⚡ LMS PENDING 응답 확인");
        log.info("🔄 LMS 전송 중... 대기");

        Thread.sleep(10000);

        log.info("✅ LMS 테스트 완료! 긴 메시지로 도착했는지 확인하세요 📱");
    }
}