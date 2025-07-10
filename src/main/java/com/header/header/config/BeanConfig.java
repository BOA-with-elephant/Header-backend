package com.header.header.config;

import com.header.header.domain.menu.dto.MenuCategoryDTO;
import com.header.header.domain.menu.dto.MenuDTO;
import com.header.header.domain.menu.entity.Menu;
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
            .setFieldMatchingEnabled(true)
            .setAmbiguityIgnored(true);

        // === 메뉴 카테고리 매핑 설정 ===
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

        // === 메뉴 매핑 설정 ===
        /**
         * 3. Menu(Entity) → MenuDTO
         *    - 연관된 MenuCategory 엔티티의 복합키 및 필드를 분해하여 DTO 필드에 매핑
         *    - 복합키: categoryCode, shopCode
         *    - 추가 필드: categoryName, menuColor
         */
        modelMapper.createTypeMap(Menu.class, MenuDTO.class)
            .addMappings(mapper -> {
                mapper.map(src -> src.getMenuCategory().getId().getCategoryCode(),
                    MenuDTO::setCategoryCode);
                mapper.map(src -> src.getMenuCategory().getId().getShopCode(),
                    MenuDTO::setShopCode);
                mapper.map(src -> src.getMenuCategory().getCategoryName(),
                    MenuDTO::setCategoryName);
                mapper.map(src -> src.getMenuCategory().getMenuColor(), MenuDTO::setMenuColor);
            });

        /**
         * 4. MenuDTO → Menu(Entity)
         *    - menuCategory는 서비스 계층에서 별도로 조회 및 주입하므로 매핑 대상에서 제외
         *    - ModelMapper가 menuCategory 필드를 건드리지 않도록 후처리(post converter)에서 명시적으로 제외 처리
         */
        modelMapper.createTypeMap(MenuDTO.class, Menu.class)
            .setPostConverter(context -> {
                Menu menu = context.getDestination();
                return menu;
            });

        return modelMapper;
    }
}