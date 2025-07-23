package com.header.header.domain.message.repository;

import com.header.header.domain.message.entity.MessageTemplate;
import com.header.header.domain.message.enums.TemplateType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, Integer> {

    /* 정보성 템플릿 리스트 가져오기 */
    List<MessageTemplate> findByTemplateType(TemplateType templateType);

    /* shopId에 해당하는 광고성 템플릿 리스트 가져오기. */
    List<MessageTemplate> findByShopCodeAndTemplateType(Integer shopCode, TemplateType templateType);

    /* 탬플릿 코드로 템플릿 가져오기 */
    Optional<MessageTemplate> findByTemplateCode(Integer templateCode);

    Optional<MessageTemplate> findByShopCodeAndTemplateCode(Integer shopCode, Integer templateCode);
}
