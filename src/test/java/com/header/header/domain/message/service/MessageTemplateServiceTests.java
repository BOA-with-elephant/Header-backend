package com.header.header.domain.message.service;

import com.header.header.domain.message.dto.MessageTemplateDTO;
import com.header.header.domain.message.enums.TemplateType;
import com.header.header.domain.message.exception.InvalidTemplateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class MessageTemplateServiceTests {

    @Autowired
    private MessageTemplateService messageTemplateService;

    private MessageTemplateDTO testTemplate;

    @BeforeEach
    void setUp(){
        MessageTemplateDTO createDTO  = new MessageTemplateDTO();
        createDTO .setShopCode(2);
        createDTO .setTemplateType(TemplateType.PROMOTIONAL);
        createDTO .setTemplateContent("안녕하세요 {고객명}님! {서비스명}이 현재 할인중 입니다.");

        testTemplate  = messageTemplateService.createPromotionalTemplate(createDTO );
    }

    // Create Template Test
    @Test
    @DisplayName("광고성 템플릿 생성 성공")
    void createPromotionalTemplate_Success(){
        //Given
        MessageTemplateDTO templateDTO = new MessageTemplateDTO();
        templateDTO.setShopCode(1);
        templateDTO.setTemplateType(TemplateType.PROMOTIONAL);
        templateDTO.setTemplateContent("안녕하세요 {고객명}님! {서비스명}이 현재 할인중 입니다.");

        //When(실행)
        MessageTemplateDTO result = messageTemplateService.createPromotionalTemplate(templateDTO);

        //Then(검증)
        assertNotNull(result);
        assertNotNull(result.getTemplateCode());
        assertEquals(1, result.getShopCode());
        assertEquals(TemplateType.PROMOTIONAL, result.getTemplateType());
    }

    @Test
    @DisplayName("필수값 누락 시 예외 발생")
    void createPromotionalTemplate_MissingRequired_ThrowException() {
        // Given
        MessageTemplateDTO templateDTO = new MessageTemplateDTO();
        //shopCode 누락
        templateDTO.setTemplateType(TemplateType.PROMOTIONAL);
        templateDTO.setTemplateContent("내용");

        // When & Then
        InvalidTemplateException exception = assertThrows(
                InvalidTemplateException.class,  // 예상 예외 타입
                () -> messageTemplateService.createPromotionalTemplate(templateDTO)  // 실행할 코드
        );

        // 예외 메시지 검증
        assertTrue(exception.getMessage().contains("샵"));
    }

    @Test
    @DisplayName("유효하지 않은 템플릿 양식일 경우 예외 발생")
    void createPromotionalTemplate_UnValidPlaceHolder_ThrowException() {
        // Given
        MessageTemplateDTO templateDTO = new MessageTemplateDTO();
        templateDTO.setShopCode(2);
        templateDTO.setTemplateType(TemplateType.PROMOTIONAL);
        templateDTO.setTemplateContent("안녕하세요 {고객명}님! {상품명} 예약이 완료되었습니다.");

        // When & Then
        assertThatThrownBy(() -> messageTemplateService.createPromotionalTemplate(templateDTO))
                .isInstanceOf(InvalidTemplateException.class)
                .hasMessageContaining("플레이스홀더");
    }
    
    // Modify Template Test
    @Test
    @DisplayName("광고성 템플릿 수정 성공")
    void modifyPromotionalTemplate_Success(){
        //Given
        // 수정할 새로운 내용
        String newContent = "안녕하세요 {고객명}님! {서비스명}이 현재 할인중 입니다. 최대 10퍼 할인 중!";
        testTemplate.setTemplateContent(newContent);

        //When(실행)
        MessageTemplateDTO result  = messageTemplateService.modifyMessageTemplateContent(testTemplate);

        //Then(검증)
        assertEquals("안녕하세요 {고객명}님! {서비스명}이 현재 할인중 입니다. 최대 10퍼 할인 중!",result.getTemplateContent());
        assertNotEquals("안녕하세요 {고객명}님! {서비스명}이 현재 할인중 입니다.", result.getTemplateContent());
    }

    @Test
    @DisplayName("필수값 누락 시 예외 발생")
    void modifyPromotionalTemplate_MissingRequired_ThrowException() {
        //Given
        // 수정할 새로운 내용
        String newContent = "";
        testTemplate.setTemplateContent(newContent);

        // When & Then
        assertThatThrownBy(() -> messageTemplateService.modifyMessageTemplateContent(testTemplate))
                .isInstanceOf(InvalidTemplateException.class)
                .hasMessageContaining("필수값 누락");
    }

    @Test
    @DisplayName("유효하지 않은 템플릿 양식일 경우 예외 발생")
    void modifyPromotionalTemplate_UnValidPlaceHolder_ThrowException() {
        // Given
        MessageTemplateDTO templateDTO = new MessageTemplateDTO();
        templateDTO.setShopCode(2);
        templateDTO.setTemplateType(TemplateType.PROMOTIONAL);
        templateDTO.setTemplateContent("안녕하세요 {고객명}님! {상품명} 예약이 완료되었습니다.");

        // When & Then
        InvalidTemplateException exception = assertThrows(
                InvalidTemplateException.class,  // 예상 예외 타입
                () -> messageTemplateService.modifyMessageTemplateContent(templateDTO)  // 실행할 코드
        );


        // 예외 메시지 검증
        assertTrue(exception.getMessage().contains("플레이스홀더"));
    }
    
}
