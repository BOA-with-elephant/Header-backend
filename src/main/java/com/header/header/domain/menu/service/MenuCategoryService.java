package com.header.header.domain.menu.service;

import com.header.header.domain.menu.dto.MenuCategoryDTO;
import com.header.header.domain.menu.entity.MenuCategory;
import com.header.header.domain.menu.entity.MenuCategoryId;
import com.header.header.common.exception.NotFoundException;
import com.header.header.domain.menu.repository.MenuCategoryRepository;
import com.header.header.domain.menu.repository.MenuRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 메뉴 카테고리 관련 비즈니스 로직을 처리하는 서비스 클래스.
 *
 * 모든 기능은 '특정 샵' 중심으로 동작하며, 샵코드를 필수로 받아서 처리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 클래스 레벨에서 모든 메서드를 읽기 전용 트랜잭션으로 설정 (성능 최적화)
public class MenuCategoryService {

    // final 선언 -> 불변성 보장 및 의존성 주입 시 필수 요소로 지정
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuRepository menuRepository;
    private final ModelMapper modelMapper;

    /**
     * 특정 샵의 전체 메뉴 카테고리 조회
     *
     * @param shopCode 조회할 샵의 코드 (필수)
     * @return 해당 샵의 모든 메뉴 카테고리 DTO 리스트
     */
    public List<MenuCategoryDTO> findAllMenuCategoriesByShopCode(Integer shopCode) {

        List<MenuCategory> categoryList = menuCategoryRepository.findAllByShopCodeOrderByCategoryCodeAsc(
            shopCode);

        return toDTOList(categoryList);
    }

    /**
     * 특정 샵의 단일 메뉴 카테고리 조회
     *
     * @param categoryCode 카테고리 코드
     * @param shopCode     샵 코드
     * @return 조회된 메뉴 카테고리 DTO
     * @throws NotFoundException 해당 카테고리가 존재하지 않을 때
     */
    public MenuCategoryDTO findMenuCategoryById(Integer categoryCode, Integer shopCode) {

        MenuCategoryId id = new MenuCategoryId(categoryCode, shopCode);
        MenuCategory menuCategory = menuCategoryRepository.findById(id)
            .orElseThrow(() -> NotFoundException.category(categoryCode, shopCode));

        return toDTO(menuCategory);
    }

    /**
     * 특정 샵의 활성화된 카테고리만 조회
     *
     * @param shopCode 조회할 샵의 코드
     * @return 해당 샵의 활성화된 메뉴 카테고리 DTO 리스트
     */
    public List<MenuCategoryDTO> findActiveMenuCategoriesByShopCode(Integer shopCode) {

        List<MenuCategory> menuCategoryList = menuCategoryRepository.findActiveMenuCategoriesByShopCode(
            shopCode);

        return toDTOList(menuCategoryList);
    }

    /**
     * 특정 샵의 카테고리명으로 검색
     *
     * @param categoryName 검색할 카테고리명 (부분 검색 가능)
     * @param shopCode     검색할 샵의 코드
     * @return 검색된 메뉴 카테고리 DTO 리스트
     */
    public List<MenuCategoryDTO> findMenuCategoriesByNameAndShopCode(String categoryName,
        Integer shopCode) {

        List<MenuCategory> menuCategoryList = menuCategoryRepository.findByCategoryNameContainingAndShopCode(
            categoryName, shopCode);

        return toDTOList(menuCategoryList);
    }

    /**
     * 메뉴 카테고리 생성
     *
     * @param menuCategoryDTO 생성할 카테고리 정보가 담긴 DTO
     * @param shopCode        카테고리를 생성할 샵의 코드
     * @return 생성된 메뉴 카테고리 DTO
     * @throws IllegalArgumentException 유효성 검사 실패 또는 이미 존재하는 카테고리일 때
     */
    @Transactional
    public MenuCategoryDTO createMenuCategory(MenuCategoryDTO menuCategoryDTO, Integer shopCode) {

        // DTO에 샵코드 설정
        menuCategoryDTO.setShopCode(shopCode);

        // 카테고리 코드가 null이거나 0이면 자동 생성
        if (menuCategoryDTO.getCategoryCode() == null || menuCategoryDTO.getCategoryCode() == 0) {
            Integer nextCategoryCode = menuCategoryRepository.findNextCategoryCodeByShopCode(shopCode);
            menuCategoryDTO.setCategoryCode(nextCategoryCode);
        }

        // 중복 확인
        if (menuCategoryRepository.existsByIdCategoryCodeAndIdShopCode(menuCategoryDTO.getCategoryCode(), shopCode)) {
            throw new IllegalArgumentException(
                String.format("이미 존재하는 카테고리입니다. CategoryCode: %d, ShopCode: %d",
                    menuCategoryDTO.getCategoryCode(), shopCode));
        }

        MenuCategoryId id = new MenuCategoryId(menuCategoryDTO.getCategoryCode(), shopCode);

        MenuCategory menuCategory = MenuCategory.builder()
            .id(id)
            .categoryName(menuCategoryDTO.getCategoryName())
            .menuColor(menuCategoryDTO.getMenuColor())
            .isActive(menuCategoryDTO.getIsActive() != null ? menuCategoryDTO.getIsActive() : true)
            .build();

        MenuCategory savedMenuCategory = menuCategoryRepository.save(menuCategory);

        return toDTO(savedMenuCategory);
    }

    /**
     * 메뉴 카테고리 수정
     *
     * @param categoryCode    수정할 카테고리 코드
     * @param shopCode        수정할 카테고리가 속한 샵 코드
     * @param menuCategoryDTO 수정할 정보가 담긴 DTO
     * @return 수정된 메뉴 카테고리 DTO
     * @throws NotFoundException 해당 카테고리가 존재하지 않을 때
     */
    @Transactional
    public MenuCategoryDTO updateMenuCategory(Integer categoryCode, Integer shopCode,
        MenuCategoryDTO menuCategoryDTO) {

        MenuCategoryId id = new MenuCategoryId(categoryCode, shopCode);
        MenuCategory existingMenuCategory = menuCategoryRepository.findById(id)
            .orElseThrow(() -> NotFoundException.category(categoryCode, shopCode));


        MenuCategory updatedMenuCategory = MenuCategory.builder()
            .id(existingMenuCategory.getId())
            .categoryName(menuCategoryDTO.getCategoryName() != null ?
                menuCategoryDTO.getCategoryName() : existingMenuCategory.getCategoryName())
            .menuColor(menuCategoryDTO.getMenuColor() != null ?
                menuCategoryDTO.getMenuColor() : existingMenuCategory.getMenuColor())
            .isActive(menuCategoryDTO.getIsActive() != null ?
                menuCategoryDTO.getIsActive() : existingMenuCategory.getIsActive())
            .build();

        MenuCategory savedMenuCategory = menuCategoryRepository.save(updatedMenuCategory);

        return toDTO(savedMenuCategory);
    }

    /**
     * 메뉴 카테고리 삭제 (논리적 삭제 - isActive를 false로 변경)
     * 해당 카테고리의 모든 메뉴들도 함께 비활성화 (벌크 업데이트 방식)
     *
     * @param categoryCode 삭제할 카테고리 코드
     * @param shopCode     삭제할 카테고리가 속한 샵 코드
     * @throws NotFoundException 해당 카테고리가 존재하지 않을 때
     */
    @Transactional
    public void deleteMenuCategoryWithBulkUpdate(Integer categoryCode, Integer shopCode) {

        MenuCategoryId id = new MenuCategoryId(categoryCode, shopCode);

        // 1. 기존 카테고리 조회
        MenuCategory existingMenuCategory = menuCategoryRepository.findById(id)
            .orElseThrow(() -> NotFoundException.category(categoryCode, shopCode));

        // 2. 해당 카테고리의 모든 활성화된 메뉴들을 한 번에 비활성화 (벌크 업데이트)
        int updatedMenuCount = menuRepository.updateActiveStatusByCategoryCodeAndShopCode(
            false, categoryCode, shopCode, true); // 마지막 파라미터는 현재 활성화된 메뉴들만 대상

        // 3. 카테고리를 false로 변경하여 논리적 삭제
        MenuCategory deletedMenuCategory = MenuCategory.builder()
            .id(id)
            .categoryName(existingMenuCategory.getCategoryName())
            .menuColor(existingMenuCategory.getMenuColor())
            .isActive(false)  // 논리적 삭제
            .build();

        menuCategoryRepository.save(deletedMenuCategory);
    }


    /* 공통 로직 정리 */
    /**
     * 엔티티 → DTO 변환
     */
    private MenuCategoryDTO toDTO(MenuCategory menuCategory) {
        return modelMapper.map(menuCategory, MenuCategoryDTO.class);
    }

    /**
     * 엔티티 리스트 → DTO 리스트 변환
     */
    private List<MenuCategoryDTO> toDTOList(List<MenuCategory> menuCategoryList) {
        return menuCategoryList.stream()
            .map(this::toDTO)
            .toList();
    }

}