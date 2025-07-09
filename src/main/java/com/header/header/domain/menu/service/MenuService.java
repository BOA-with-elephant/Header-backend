package com.header.header.domain.menu.service;

import com.header.header.domain.menu.dto.MenuDTO;
import com.header.header.domain.menu.entity.Menu;
import com.header.header.domain.menu.entity.MenuCategory;
import com.header.header.domain.menu.entity.MenuCategoryId;
import com.header.header.domain.menu.exception.NotFoundException;
import com.header.header.domain.menu.repository.MenuRepository;
import com.header.header.domain.menu.repository.MenuCategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 메뉴 관련 비즈니스 로직을 처리하는 서비스 클래스
 *
 * 모든 기능은 '특정 샵' 중심으로 동작하며, 샵코드를 필수로 받아서 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final ModelMapper modelMapper;

    /**
     * 메뉴 생성
     *
     * @param menuDTO 생성할 메뉴 정보 (menuName, menuPrice, estTime, categoryCode, shopCode 필수)
     * @return 생성된 메뉴 정보 (menuCode 포함)
     * @throws IllegalArgumentException 유효성 검사 실패 시
     * @throws NotFoundException        메뉴 카테고리를 찾을 수 없을 시
     */
    @Transactional
    public MenuDTO createMenu(MenuDTO menuDTO) {
        // 기본값 설정 보완 (DTO에서 null로 들어온 경우 대비)
        if (menuDTO.getIsActive() == null) {
            menuDTO.setIsActive(true);
        }

        // 2. 메뉴 카테고리 존재 확인 (NotFoundException 사용)
        MenuCategoryId categoryId = new MenuCategoryId(menuDTO.getCategoryCode(), menuDTO.getShopCode());
        MenuCategory category = menuCategoryRepository.findById(categoryId)
            .orElseThrow(() -> NotFoundException.category(menuDTO.getCategoryCode(), menuDTO.getShopCode()));

        // 3. 메뉴 엔티티 생성 (빌더 패턴 사용)
        Menu menu = Menu.builder()
            .menuName(menuDTO.getMenuName())
            .menuPrice(menuDTO.getMenuPrice())
            .estTime(menuDTO.getEstTime())
            .isActive(menuDTO.getIsActive())
            .menuCategory(category)
            .build();

        return toDTO(menuRepository.save(menu));
    }

    /**
     * 메뉴 단건 조회
     *
     * @param menuCode 조회할 메뉴 코드 (Integer 타입으로 통일)
     * @return 메뉴 정보
     * @throws NotFoundException 메뉴를 찾을 수 없을 시
     */
    public MenuDTO getMenu(Integer menuCode) {
        Menu menu = menuRepository.findById(menuCode)
            .orElseThrow(() -> NotFoundException.menu(menuCode));

        return toDTO(menu);
    }

    /**
     * 특정 샵의 모든 메뉴 조회
     *
     * @param shopCode 샵 코드
     * @return 해당 샵의 모든 메뉴 목록
     */
    public List<MenuDTO> getMenusByShop(Integer shopCode) {
        return toDTOList(menuRepository.findByShopCode(shopCode));
    }

    /**
     * 특정 샵의 활성화된 메뉴만 조회
     *
     * @param shopCode 샵 코드
     * @return 해당 샵의 활성화된 메뉴 목록
     */
    public List<MenuDTO> getActiveMenusByShop(Integer shopCode) {
        return toDTOList(menuRepository.findActiveMenusByShop(shopCode));
    }

    /**
     * 특정 샵의 카테고리별 활성화된 메뉴만 조회
     *
     * @param categoryCode 카테고리 코드
     * @param shopCode     샵 코드
     * @return 해당 카테고리의 활성화된 메뉴 목록
     */
    public List<MenuDTO> getActiveMenusByCategory(Integer categoryCode, Integer shopCode) {
        return toDTOList(menuRepository.findActiveMenusByCategory(categoryCode, shopCode));
    }

    /**
     * 메뉴 수정
     *
     * @param menuCode 수정할 메뉴 코드
     * @param menuDTO  수정할 메뉴 정보
     * @return 수정된 메뉴 정보
     * @throws NotFoundException 메뉴를 찾을 수 없을 시
     */
    @Transactional
    public MenuDTO updateMenu(Integer menuCode, MenuDTO menuDTO) {
        // 1. 기존 메뉴 조회 (NotFoundException 사용)
        Menu menu = menuRepository.findById(menuCode)
            .orElseThrow(() -> NotFoundException.menu(menuCode));

        // 2. 메뉴 정보 업데이트 (엔티티의 업데이트 메소드 사용)
        menu.updateMenuInfo(
            menuDTO.getMenuName(),
            menuDTO.getMenuPrice(),
            menuDTO.getEstTime()
        );

        // 3. 활성화 상태 업데이트 (null이 아닌 경우에만)
        if (menuDTO.getIsActive() != null) {
            menu.updateActiveStatus(menuDTO.getIsActive());
        }

        return toDTO(menu);
    }

    /**
     * 메뉴 삭제 (논리적 삭제 - 비활성화)
     *
     * @param menuCode 삭제할 메뉴 코드
     * @throws NotFoundException 메뉴를 찾을 수 없을 시
     */
    @Transactional
    public void deleteMenu(Integer menuCode) {
        // 1. 기존 메뉴 조회 (NotFoundException 사용)
        Menu menu = menuRepository.findById(menuCode)
            .orElseThrow(() -> NotFoundException.menu(menuCode));

        // 2. 논리적 삭제 (비활성화 처리)
        menu.updateActiveStatus(false);
    }

    /**
     * 메뉴 이름으로 검색
     *
     * @param menuName 검색할 메뉴 이름 (부분 일치)
     * @param shopCode 검색 대상 샵 코드
     * @return 검색된 메뉴 목록
     */
    public List<MenuDTO> searchMenusByName(String menuName, Integer shopCode) {
        return toDTOList(menuRepository.findByMenuNameContainingAndShopCode(menuName, shopCode));
    }

    /* 공통 로직 정리 */
    /**
     * 엔티티 → DTO 변환
     */
    private MenuDTO toDTO(Menu menu) {
        return modelMapper.map(menu, MenuDTO.class);
    }

    /**
     * 엔티티 리스트 → DTO 리스트 변환
     */
    private List<MenuDTO> toDTOList(List<Menu> menus) {
        return menus.stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

}