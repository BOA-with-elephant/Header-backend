package com.header.header.config;

import com.header.header.domain.menu.dto.MenuCategoryDTO;
import com.header.header.domain.menu.entity.MenuCategory;
import com.header.header.domain.menu.entity.MenuCategoryId;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
            .setFieldAccessLevel(
                org.modelmapper.config.Configuration.AccessLevel.PRIVATE
            )
            .setFieldMatchingEnabled(true);

        /**
         * MenuCategory 테이블의 복합 기본키 매핑을 위한 TypeMap 설정 추가
         *
         * 1. MenuCategory(Entity) -> MenuCategoryDTO 매핑 설정
         * 복합키의 내부 필드들을 DTO의 개별 필드로 매핑하는 규칙 정의
         */
        modelMapper.createTypeMap(MenuCategory.class, MenuCategoryDTO.class)
            .addMapping(src -> src.getId().getCategoryCode(), MenuCategoryDTO::setCategoryCode)
            .addMapping(src -> src.getId().getShopCode(), MenuCategoryDTO::setShopCode);

        /**
         * 2. MenuCategoryDTO -> MenuCategory(Entity) 매핑 설정
         * DTO의 개별 필드들을 복합키로 조합하여 Entity로 변환하는 규칙 정의
         * 복잡한 객체 생성 로직이 필요하므로 커스텀 컨버터 사용
         */
        modelMapper.createTypeMap(MenuCategoryDTO.class, MenuCategory.class)
            .setConverter(context -> {
                MenuCategoryDTO menuCategoryDTO = context.getSource();

                // DTO의 categoryCode와 shopCode로 복합키 생성 (일반 생성자 사용)
                MenuCategoryId id = new MenuCategoryId(menuCategoryDTO.getCategoryCode(),
                    menuCategoryDTO.getShopCode());

                // Builder 패턴을 사용하여 Entity 객체 생성
                return MenuCategory.builder()
                    .id(id)
                    .categoryName(menuCategoryDTO.getCategoryName())
                    .menuColor(menuCategoryDTO.getMenuColor())
                    .isActive(menuCategoryDTO.getIsActive())
                    .build();
            });

        return modelMapper;
    }
}