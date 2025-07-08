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

import java.util.List;

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
        createDTO .setTemplateTitle("서비스 할인 안내");
        createDTO .setTemplateType(TemplateType.PROMOTIONAL);
        createDTO .setTemplateContent("안녕하세요 {고객명}님! {서비스명}이 현재 할인중 입니다.");

        testTemplate  = messageTemplateService.createPromotionalTemplate(createDTO );
    }

    // Get Informational Template List
    @Test
    @DisplayName("정보성 템플릿 리스트 조회 성공")
    void readInformationalTemplates_Success(){
        List<MessageTemplateDTO> templates = messageTemplateService.getSystemProvidedTemplates();

        System.out.println("templates List:" + templates);

        assertNotNull(templates);
    }

    @Test
    @DisplayName("광고성 템플릿 리스트 조회 성공")
    void readPromotionalTemplates_Success(){
        List<MessageTemplateDTO> templates = messageTemplateService.getPromotionalTemplatesByShop(2);

        System.out.println("templates List:" + templates);

        assertNotNull(templates);
    }

    @Test
    @DisplayName("광고성 템플릿 리스트 조회 실패")
    void readPromotionalTemplates_NonExistent_ThrowException(){
        // Given
        Integer nonExistentId = 99999;

        // When & Then
        assertThatThrownBy(() -> messageTemplateService.getPromotionalTemplatesByShop(nonExistentId))
                .isInstanceOf(InvalidTemplateException.class)
                .hasMessageContaining("유효하지 않은 템플릿 코드");
    }

    // Create Template Test
    @Test
    @DisplayName("광고성 템플릿 생성 성공")
    void createPromotionalTemplate_Success(){
        //When(실행)
        MessageTemplateDTO result = messageTemplateService.createPromotionalTemplate(testTemplate);

        //Then(검증)
        assertNotNull(result);
        assertNotNull(result.getTemplateCode());
        assertEquals(2, result.getShopCode());
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
    @DisplayName("공백만 있는 내용일 때 예외 발생")
    void modifyPromotionalTemplate_WhitespaceContent_ThrowException() {
        // Given
        testTemplate.setTemplateContent("   ");  // 공백만

        // When & Then
        assertThatThrownBy(() -> messageTemplateService.modifyMessageTemplateContent(testTemplate))
                .isInstanceOf(InvalidTemplateException.class)
                .hasMessageContaining("필수값 누락");
    }

    @Test
    @DisplayName("유효하지 않은 템플릿 양식일 경우 예외 발생")
    void modifyPromotionalTemplate_UnValidPlaceHolder_ThrowException() {
        // Given
        testTemplate.setTemplateContent("안녕하세요 {고객명}님! {상품명} 예약이 완료되었습니다.");

        // When & Then
        assertThatThrownBy(() -> messageTemplateService.modifyMessageTemplateContent(testTemplate))
                .isInstanceOf(InvalidTemplateException.class)
                .hasMessageContaining("플레이스홀더");
    }

    // Delete Template Test
    @Test
    @DisplayName("템플릿 삭제 성공")
    void deleteMessageTemplate_Success(){
        // Given
        Integer templateCode = testTemplate.getTemplateCode();
        Integer shopCode = testTemplate.getShopCode();

        // When - 삭제 실행 (예외 발생하지 않음)
        assertDoesNotThrow(() -> messageTemplateService.deleteMessageTemplate(templateCode,shopCode));

        // Then - 삭제 후 조회 시 예외 발생해야 함
        assertThatThrownBy(() -> messageTemplateService.getTemplate(templateCode))
                .isInstanceOf(InvalidTemplateException.class)
                .hasMessageContaining("템플릿을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("존재하지 않는 템플릿 삭제 시 예외 발생")
    void deleteMessageTemplate_NonExistent_ThrowException(){
        // Given
        Integer nonExistentId = 99999;
        Integer shopCode = 1;

        // When & Then
        assertThatThrownBy(() -> messageTemplateService.deleteMessageTemplate(nonExistentId, shopCode))
                .isInstanceOf(InvalidTemplateException.class)
                .hasMessageContaining("템플릿을 찾을 수 없습니다");
    }

    @Test
    @DisplayName("다른 샵의 템플릿 삭제시 예외 발생")
    void deleteMessageTemplate_UnauthorizedShop_ThrowException(){
        // Given
        Integer templateCode = testTemplate.getTemplateCode();
        Integer wrongShopCode = 1; // 다른 샵 코드

        // When & Then
        assertThatThrownBy(() -> messageTemplateService.deleteMessageTemplate(templateCode, wrongShopCode))
                .isInstanceOf(InvalidTemplateException.class)
                .hasMessageContaining("권한 오류");
    }

    
}
