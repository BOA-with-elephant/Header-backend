package com.header.header.domain.message.validator;

import com.header.header.domain.message.dto.PlaceholderDTO;
import com.header.header.domain.message.exception.ValidationResult;
import com.header.header.domain.message.enums.MessagePlaceholder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TemplateValidator {

    // 템플릿 유효성 검사
    public ValidationResult validateTemplate(String templateContent){
        Set<String> foundPlaceholders = MessagePlaceholder.extractPlaceholders(templateContent);
        Set<String> validPlaceholders = MessagePlaceholder.getAllPlaceholders();

        List<String> invalidPlaceholders = foundPlaceholders.stream()
                .filter(placeholder -> !validPlaceholders.contains(placeholder))
                .toList();

        if(invalidPlaceholders.isEmpty()){
            return ValidationResult.success();
        }else{
            return ValidationResult.failure("유효하지 않은 플레이스 홀더: " + String.join(", ", invalidPlaceholders));
        }
    }

    // 사용 가능한 플레이스 홀더 목록 반환
    public List<PlaceholderDTO> getAvailablePlaceholders() {
        return Arrays.stream(MessagePlaceholder.values())
                .map(p -> new PlaceholderDTO(p.getPlaceholder()))
                .collect(Collectors.toList());

    }
}
