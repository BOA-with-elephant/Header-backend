package com.header.header.domain.message.service;

import com.header.header.domain.message.dto.MessageTemplateDTO;
import com.header.header.domain.message.enitity.MessageTemplate;
import com.header.header.domain.message.repository.MessageTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageTemplateService {

    private final MessageTemplateRepository messageTemplateRepository;
    private final ModelMapper modelMapper;

    /* comment. MessageTemplate 중 NAddType은 ShopId가 Null이고 정보성 기본 제공 템플릿.
     *   AddType은 샵 별 커스텀 템플릿이고 광고성 템플릿이다. */

    /* comment. [Read] 정보성 템플릿 리스트 가져오기 templateType = NAd */
    public List<MessageTemplateDTO> getSystemProvidedTemplates(String templateType){
        List<MessageTemplate> messageTemplates = messageTemplateRepository.findMessageTemplatesByTemplateType(templateType);

        return messageTemplates.stream()
                .map(messageTemplate -> modelMapper.map(messageTemplate, MessageTemplateDTO.class))
                .toList();
    }

    /* comment. [Read] parameter shopId에 해당하는 광고성 템플릿 리스트 가져오기. templateType = Ad*/
    public List<MessageTemplateDTO> getPromotionalTemplatesByShop(Integer shopCode){
        List<MessageTemplate> messageTemplates = messageTemplateRepository.findMessageTemplatesByShopCodeAndTemplateType(shopCode, "Ad");

        return messageTemplates.stream()
                .map( messageTemplate -> modelMapper.map(messageTemplate, MessageTemplateDTO.class))
                .toList();
    }

    /* comment. [Create] 광고성 템플릿 생성하기 */
    @Transactional
    public void createPromotionalTemplate(MessageTemplateDTO messageTemplateDTO){
        // todo. ⭐ 유효한 템플릿 형식인지 검증 하는 로직 필요
        messageTemplateRepository.save(modelMapper.map(messageTemplateDTO, MessageTemplate.class));
    }

    /* comment. [Update] 광고성 템플릿 메세지 내용 수정하기 */
    @Transactional
    public void modifyMessageTemplateContent(MessageTemplateDTO messageTemplateDTO){
        // todo. 👾 템플릿 못찾을 경우 예외처리 제대로 하기
        MessageTemplate foundMessageTemplate = messageTemplateRepository.findById(messageTemplateDTO.getTempleteCode()).orElseThrow(IllegalAccessError::new);
        // todo. ⭐ 유효한 템플릿 형식인지 검증 하는 로직 필요
        foundMessageTemplate.modifyMessageTemplateContent(messageTemplateDTO.getTemplateContent());
    }

    /* comment. [Delete] paramter shopId, parameter templateCode에 해당하는 광고성 템플릿 삭제하기 */
    public void deleteMessageTemplate(Integer messageTemplateCode){
        // todo. 👾 템플릿 못찾을 경우 예외처리 제대로 하기
        messageTemplateRepository.deleteById(messageTemplateCode);
    }

    // todo. == 비즈니스 검증 메서드 ==

}
