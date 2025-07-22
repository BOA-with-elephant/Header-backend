package com.header.header.domain.message.service;

import com.header.header.domain.message.dto.MessageTemplateDTO;
import com.header.header.domain.message.dto.MessageTemplateResponse;
import com.header.header.domain.message.dto.MessageTemplateSimpleDto;
import com.header.header.domain.message.entity.MessageTemplate;
import com.header.header.domain.message.enums.TemplateType;
import com.header.header.domain.message.exception.InvalidTemplateException;
import com.header.header.domain.message.exception.ValidationResult;
import com.header.header.domain.message.repository.MessageTemplateRepository;
import com.header.header.domain.message.validator.TemplateValidator;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageTemplateService {

    private final MessageTemplateRepository messageTemplateRepository;
    private final ModelMapper modelMapper;
    private final TemplateValidator templateValidator;

    /**
     * 정보성 + 광고성 템플릿을 모두 조회하여 클라이언트 응답 형식으로 반환합니다.
     * @param shopCode 샵 코드
     * @return List<MessageTemplateResponse> 클라이언트 응답 형식으로 반환
     */
    public List<MessageTemplateResponse> getAllTypeTemplateList(Integer shopCode){
        // 시스템 제공 informational 템플릿 조회
        List<MessageTemplateDTO> systemProvidedTemplates =
                getSystemProvidedTemplates();

        // 샵 코드로 조회된 promotional 템플릿 조회
        List<MessageTemplateDTO> promotionalTemplates =
                getPromotionalTemplatesByShop(shopCode);

        // 변환
        List<MessageTemplateSimpleDto> informationalDtos = systemProvidedTemplates.stream()
                .map(dto -> new MessageTemplateSimpleDto(dto.getTemplateCode(),dto.getTemplateTitle(), dto.getTemplateContent()))
                .collect(Collectors.toList());

        List<MessageTemplateSimpleDto> promotionalDtos = promotionalTemplates.stream()
                .map(dto -> new MessageTemplateSimpleDto(dto.getTemplateCode(),dto.getTemplateTitle(), dto.getTemplateContent()))
                .collect(Collectors.toList());

        // 응답 리스트 생성 및 반환
        return List.of(
                new MessageTemplateResponse("informational", informationalDtos),
                new MessageTemplateResponse("promotional", promotionalDtos)
        );
    }

    /**
     * 정보성 템플릿 리스트 가져오기
     *
     * @return List<MessageTemplateDTO>
     */
    public List<MessageTemplateDTO> getSystemProvidedTemplates(){
        List<MessageTemplate> messageTemplates = messageTemplateRepository.findByTemplateType(TemplateType.INFORMATIONAL);

        return messageTemplates.stream()
                .map(messageTemplate -> modelMapper.map(messageTemplate, MessageTemplateDTO.class))
                .toList();
    }

    /**
     * 광고성 템플릿 리스트 가져오기
     *
     * @param shopCode 어떤 샵의 템플릿을 가져올지
     * @return List<MessageTemplateDTO>
     */
    public List<MessageTemplateDTO> getPromotionalTemplatesByShop(Integer shopCode){
        validateShopExists(shopCode); // comment. 해당 검증은 shop 시스템에서 가져오거나 controller에서 interceptor을 통해 처리해도 될 듯.

        List<MessageTemplate> messageTemplates = messageTemplateRepository.findByShopCodeAndTemplateType(shopCode, TemplateType.PROMOTIONAL);

        return messageTemplates.stream()
                .map( messageTemplate -> modelMapper.map(messageTemplate, MessageTemplateDTO.class))
                .toList();
    }


    /* comment. for test */
    public void getTemplate(Integer templateCode) {
        modelMapper.map(findTemplateOrThrow(templateCode), MessageTemplateDTO.class);
    }


    /**
     * 광고성 템플릿 생성
     *
     * @param templateDTO 생성할 템플릿 DTO
     * @return MessageTemplateDTO
     */
    @Transactional
    public MessageTemplateDTO createPromotionalTemplate(MessageTemplateDTO templateDTO){
        // 1. 기본 필드 검증 : 비어있는 값 있는지 확인.
        validateBasicFields(templateDTO);
        
        // 2. 비즈니스 로직 검증 : 광고성 템플릿만 사용자가 생성 가능.
        validateBusinessRules(templateDTO);

        // 3. 플레이스홀더 유효성 검증 : 플레이스 홀더에 올바른 제공되는 키를 넣었는지 확인.
        validateTemplatePalceholder(templateDTO.getTemplateContent());

        // Save
        MessageTemplate savedTemplate = messageTemplateRepository
                .save(modelMapper.map(templateDTO, MessageTemplate.class));

        return modelMapper.map(savedTemplate, MessageTemplateDTO.class);
    }

    /**
     * 광고성 템플릿 수정
     *
     * @param templateDTO 생성할 템플릿 DTO
     * @return MessageTemplateDTO
     */
    @Transactional
    public MessageTemplateDTO modifyMessageTemplateContent(MessageTemplateDTO templateDTO){
        MessageTemplate foundMessageTemplate = findTemplateOrThrow(templateDTO.getTemplateCode());

        validateBasicFields(templateDTO); // 기본 필드 검증
        validateModifiable(foundMessageTemplate); // 수정할 수 있는 템플릿인지 검증
        validateTemplatePalceholder(templateDTO.getTemplateContent()); // 템플릿 placeholder 검증
        
        foundMessageTemplate.modifyMessageTemplate(templateDTO.getTemplateTitle(),templateDTO.getTemplateContent());

        return modelMapper.map(foundMessageTemplate, MessageTemplateDTO.class);
    }

    /**
     * 광고성 템플릿 삭제
     *
     * @param templateCode 삭제할 템플릿 DTO
     * @param shopCode 샵 코드
     */
    public void deleteMessageTemplate(Integer templateCode, Integer shopCode){
        MessageTemplate foundMessageTemplate = findTemplateOrThrow(templateCode);

        validateOwnership(shopCode, templateCode);  // 권한 검증 추가
        validateModifiable(foundMessageTemplate);

        messageTemplateRepository.deleteById(templateCode);
    }

    // == 비즈니스 검증 메서드 ==
    private void validateShopExists(Integer shopCode) {
        messageTemplateRepository.findById(shopCode)
                .orElseThrow(() -> InvalidTemplateException.notFound("샵을 찾을 수 없습니다."));
    }

    private void validateOwnership(Integer shopCode, Integer templateCode) {
        messageTemplateRepository.findByShopCodeAndTemplateCode(shopCode, templateCode)
                .orElseThrow(() -> InvalidTemplateException.unauthorized("해당 샵의 템플릿이 아닙니다."));
    }

    protected MessageTemplate findTemplateOrThrow(Integer templateCode) {
        return messageTemplateRepository.findByTemplateCode(templateCode)
                .orElseThrow(() -> InvalidTemplateException.notFound("템플릿을 찾을 수 없습니다."));
        
    }

    private void validateModifiable(MessageTemplate template) {
        if (!template.getTemplateType().isUserManageable()) {
            throw InvalidTemplateException.unauthorized("시스템 템플릿은 수정할 수 없습니다");
        }
    }

    // 기본 필드 검증 메서드
    private void validateBasicFields(MessageTemplateDTO templateDTO) {
        if (templateDTO.getShopCode() == null) {
            throw InvalidTemplateException.missingRequired("샵 코드가 필요합니다.");
        }

        if (templateDTO.getTemplateContent() == null || templateDTO.getTemplateContent().trim().isEmpty()) {
            throw InvalidTemplateException.missingRequired("템플릿 내용을 입력해주세요.");
        }

        if (templateDTO.getTemplateType() == null) {
            throw InvalidTemplateException.missingRequired("템플릿 타입을 선택해주세요.");
        }
    }

    // 비즈니스 로직 검증
    private void validateBusinessRules(MessageTemplateDTO templateDTO){
        if(!templateDTO.getTemplateType().isUserManageable()){
            throw InvalidTemplateException.invalidType("생성할 수 없는 템플릿 타입입니다.");
        }
    }

    // 템플릿 플레이스 홀더 검증
    private void validateTemplatePalceholder(String content){
        ValidationResult validationResult = templateValidator.validateTemplate(content);
        if(!validationResult.isValid()){
            throw InvalidTemplateException.invalidPlaceholder(validationResult.getErrorMessage());
        }

    }

    private MessageTemplateSimpleDto toSimpleDto(MessageTemplateDTO dto) {
        return new MessageTemplateSimpleDto(dto.getTemplateCode(),dto.getTemplateTitle(), dto.getTemplateContent());
    }
}
