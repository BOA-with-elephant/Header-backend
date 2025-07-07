package com.header.header.domain.message.repository;

import com.header.header.domain.message.enitity.MessageTemplate;
import com.header.header.domain.message.enums.TemplateType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

public interface MessageTemplateRepository extends JpaRepository<MessageTemplate, Integer> {

    /* 정보성 템플릿 리스트 가져오기 */
    List<MessageTemplate> findMessageTemplatesByTemplateType(TemplateType templateType);

    /* parameter shopId에 해당하는 광고성 템플릿 리스트 가져오기. */
    List<MessageTemplate> findMessageTemplatesByShopCodeAndTemplateType(Integer shopCode, TemplateType templateType);

    Optional<MessageTemplate> findByTemplateCode(Integer templateCode);
}
