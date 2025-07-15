package com.header.header.domain.message.external.coolsms;

import com.header.header.config.CoolSmsApiConfig;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.exception.NurigoMessageNotReceivedException;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CoolSmsService {

    private final DefaultMessageService messageService;

    public CoolSmsService(CoolSmsApiConfig config){
        this.messageService = NurigoApp.INSTANCE.initialize(
                config.getApiKey(),
                config.getApiSecret(),
                config.getBaseUrl()
        );
        log.info("CoolSMS SDK 초기화 완료");
    }

    /**
     * SMS 전송 (예제 코드 기반)
     */
    public void sendSms(String from, String to, String text) {

        log.info("📱 SMS 전송 시작 - To: {}", to);

        try {
            // 예제 코드와 동일한 방식
            net.nurigo.sdk.message.model.Message message =
                    new net.nurigo.sdk.message.model.Message();
            message.setFrom(from);
            message.setTo(to);
            message.setText(text);

            // SDK로 전송
            messageService.send(message);

            log.info("✅ SMS 전송 성공 - To: {}", to);

        } catch (NurigoMessageNotReceivedException exception) {
            log.error("❌ SMS 발송 실패 - 실패 목록: {}", exception.getFailedMessageList());
            log.error("❌ 에러 메시지: {}", exception.getMessage());
            throw new RuntimeException("SMS 발송 실패: " + exception.getMessage());

        } catch (Exception exception) {
            log.error("❌ SMS 전송 중 예외 발생", exception);
            throw new RuntimeException("SMS 전송 실패: " + exception.getMessage());
        }
    }

    /**
     * LMS 전송 (제목 포함)
     */
    public void sendLms(String from, String to, String text, String subject) {

        log.info("📱 LMS 전송 시작 - To: {}", to);

        try {
            net.nurigo.sdk.message.model.Message message =
                    new net.nurigo.sdk.message.model.Message();
            message.setFrom(from);
            message.setTo(to);
            message.setText(text);
            message.setSubject(subject);  // LMS는 제목 설정

            messageService.send(message);

            log.info("✅ LMS 전송 성공 - To: {}", to);

        } catch (NurigoMessageNotReceivedException exception) {
            log.error("❌ LMS 발송 실패 - 실패 목록: {}", exception.getFailedMessageList());
            throw new RuntimeException("LMS 발송 실패: " + exception.getMessage());

        } catch (Exception exception) {
            log.error("❌ LMS 전송 중 예외 발생", exception);
            throw new RuntimeException("LMS 전송 실패: " + exception.getMessage());
        }
    }
}
