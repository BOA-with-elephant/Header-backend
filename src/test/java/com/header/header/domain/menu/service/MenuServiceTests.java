package com.header.header.domain.menu.service;

import static org.junit.jupiter.api.Assertions.*;

import com.header.header.domain.menu.dto.MenuDTO;
import com.header.header.common.exception.NotFoundException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
@Transactional
public class MenuServiceTests {

    @Autowired
    private MenuService menuService;

    // 테스트용 데이터 상수
    private static final Integer TEST_SHOP_CODE = 1;
    private static final Integer TEST_CATEGORY_CODE = 7;
    private static final String TEST_MENU_NAME = "테스트 메뉴";
    private static final Integer TEST_MENU_PRICE = 20000;
    private static final Integer TEST_EST_TIME = 100;
    private static final boolean TEST_ISACTIVE = true;
    @Autowired
    private MenuCategoryService menuCategoryService;

    @Test
    @Order(1)
    @DisplayName("메뉴 생성 테스트 - 정상 케이스")
    @Commit
    void testCreateMenu() {
        // given
        MenuDTO menuDTO = new MenuDTO();
        menuDTO.setMenuName(TEST_MENU_NAME);
        menuDTO.setMenuPrice(TEST_MENU_PRICE);
        menuDTO.setEstTime(TEST_EST_TIME);
        menuDTO.setCategoryCode(TEST_CATEGORY_CODE);
        menuDTO.setShopCode(TEST_SHOP_CODE);
        menuDTO.setIsActive(TEST_ISACTIVE);

        // when
        MenuDTO createdMenu = menuService.createMenu(menuDTO);

        // then
        assertNotNull(createdMenu);
        assertNotNull(createdMenu.getMenuCode());
        assertEquals(TEST_MENU_NAME, createdMenu.getMenuName());
        assertEquals(TEST_MENU_PRICE, createdMenu.getMenuPrice());
        assertEquals(TEST_EST_TIME, createdMenu.getEstTime());
        assertEquals(TEST_CATEGORY_CODE, createdMenu.getCategoryCode());
        assertEquals(TEST_SHOP_CODE, createdMenu.getShopCode());
        assertTrue(createdMenu.getIsActive());

        System.out.println("생성된 메뉴: " + createdMenu);
    }

    @Test
    @Order(2)
    @DisplayName("메뉴 단건 조회 테스트")
    void testGetMenu() {
        // given
        int menuCode = 49;

        assertNotNull(menuCode, "먼저 메뉴가 생성되어야 합니다.");

        // When
        MenuDTO foundMenu = menuService.getMenu(menuCode);

        // Then
        assertNotNull(foundMenu);
        assertEquals(menuCode, foundMenu.getMenuCode());
        assertEquals(TEST_MENU_NAME, foundMenu.getMenuName());
        assertEquals(TEST_MENU_PRICE, foundMenu.getMenuPrice());

        System.out.println("조회된 메뉴: " + foundMenu);
    }

    @Test
    @Order(3)
    @DisplayName("특정 샵의 전체 메뉴 조회 테스트")
    void testGetMenusByShop() {
        // When
        List<MenuDTO> allMenusByShop = menuService.getMenusByShop(TEST_SHOP_CODE);

        // Then
        assertNotNull(allMenusByShop);
        assertTrue(allMenusByShop.size() > 0);

        // 생성한 메뉴가 포함되어 있는지 확인
        boolean containsCreatedMenu = allMenusByShop.stream()
            .anyMatch(menu -> menu.getMenuName().equals(TEST_MENU_NAME));
        assertTrue(containsCreatedMenu);

        System.out.println("샵의 전체 메뉴 개수: " + allMenusByShop.size());
        allMenusByShop.forEach(menu -> System.out.println("메뉴 정보: " + menu));
    }

    @Test
    @Order(4)
    @DisplayName("특정 샵의 활성화된 메뉴만 조회 테스트")
    void testGetActiveMenusByShop() {
        // When
        List<MenuDTO> activeMenusByShop = menuService.getActiveMenusByShop(TEST_SHOP_CODE);

        // Then
        assertNotNull(activeMenusByShop);
        assertTrue(activeMenusByShop.size() > 0);

        // 모든 메뉴가 활성화 상태인지 확인
        activeMenusByShop.forEach(menu -> assertTrue(menu.getIsActive()));

        System.out.println("샵의 활성화된 메뉴 개수: " + activeMenusByShop.size());
        activeMenusByShop.forEach(menu -> System.out.println("활성화 메뉴: " + menu));
    }

    @Test
    @Order(5)
    @DisplayName("특정 카테고리의 활성화된 메뉴만 조회 테스트")
    void testGetActiveMenusByCategory() {
        // When
        List<MenuDTO> activeMenusByCategory = menuService.getActiveMenusByCategory(
            TEST_CATEGORY_CODE, TEST_SHOP_CODE);

        // Then
        assertNotNull(activeMenusByCategory);
        assertTrue(activeMenusByCategory.size() > 0);

        // 모든 메뉴가 지정된 카테고리이고 활성화 상태인지 확인
        activeMenusByCategory.forEach(menu -> {
            assertEquals(TEST_CATEGORY_CODE, menu.getCategoryCode());
            assertEquals(TEST_SHOP_CODE, menu.getShopCode());
            assertTrue(menu.getIsActive());
        });

        System.out.println("카테고리별 활성화 메뉴 개수: " + activeMenusByCategory.size());
        activeMenusByCategory.forEach(menu -> System.out.println("카테고리별 활성화 메뉴: " + menu));
    }

    @Test
    @Order(6)
    @DisplayName("메뉴 이름으로 검색 테스트")
    void testSearchMenusByName() {
        // When
        List<MenuDTO> searchResults = menuService.searchMenusByName(TEST_MENU_NAME, TEST_SHOP_CODE);

        // Then
        assertNotNull(searchResults);
        assertTrue(searchResults.size() > 0);

        // 검색 결과에 검색어가 포함되어 있는지 확인
        searchResults.forEach(menu ->
            assertTrue(menu.getMenuName().contains(TEST_MENU_NAME)));

        System.out.println("검색된 메뉴 개수: " + searchResults.size());
        searchResults.forEach(menu -> System.out.println("검색된 메뉴: " + menu));
    }

    @Test
    @Order(7)
    @DisplayName("메뉴 수정 테스트")
    @Commit
    void testUpdateMenu() {
        // given
        int menuCode = 45;
        assertNotNull(menuCode, "먼저 메뉴가 생성되어야 합니다.");

        MenuDTO updateDTO = new MenuDTO();
        updateDTO.setMenuName("수정된 메뉴");
        updateDTO.setMenuPrice(20000);
        updateDTO.setEstTime(15);

        // When
        MenuDTO updatedMenu = menuService.updateMenu(menuCode, updateDTO);

        // Then
        assertNotNull(updatedMenu);
        assertEquals("수정된 메뉴", updatedMenu.getMenuName());
        assertEquals(20000, updatedMenu.getMenuPrice());
        assertEquals(15, updatedMenu.getEstTime());

        System.out.println("수정된 메뉴: " + updatedMenu);
    }

    @Test
    @Order(9)
    @DisplayName("메뉴 소프트 삭제 테스트")
    @Commit
    void testDeleteMenu() {
        // given
        int menuCode = 45;

        // 먼저 메뉴가 활성화 상태인지 확인
        MenuDTO existingMenu = menuService.getMenu(menuCode);
        assertNotNull(existingMenu, "먼저 메뉴가 생성되어야 합니다.");
        assertTrue(existingMenu.getIsActive(), "삭제 전 메뉴는 활성화 상태여야 합니다.");

        System.out.println("삭제 전 메뉴 상태: " + existingMenu);

        // When - 메뉴 삭제 (소프트 삭제)
        menuService.deleteMenu(menuCode);

        // Then - 메뉴는 존재하지만 비활성화 상태여야 함
        MenuDTO deletedMenu = menuService.getMenu(menuCode);
        assertNotNull(deletedMenu, "소프트 삭제된 메뉴는 여전히 존재해야 합니다.");
        assertFalse(deletedMenu.getIsActive(), "삭제된 메뉴는 비활성화 상태여야 합니다.");

        System.out.println("소프트 삭제된 메뉴: " + deletedMenu);
    }

    // 카테고리 삭제 시 메뉴도 함께 삭제되는 테스트
    @Test
    @Order(10)
    @DisplayName("카테고리 삭제 시 해당 메뉴들도 함께 비활성화 테스트")
    @Commit
    void testDeleteMenuCategoryWithMenus() {
        // given
        int categoryCode = 1;  // 테스트할 카테고리 코드
        int shopCode = 1;      // 샵 코드

        // 해당 카테고리의 활성화된 메뉴들 조회
        List<MenuDTO> activeMenusBeforeDelete = menuService.getActiveMenusByCategory(categoryCode, shopCode);
        assertFalse(activeMenusBeforeDelete.isEmpty(), "테스트할 카테고리에 활성화된 메뉴가 있어야 합니다.");

        System.out.println("삭제 전 카테고리의 활성 메뉴 개수: " + activeMenusBeforeDelete.size());
        activeMenusBeforeDelete.forEach(menu ->
            System.out.println("- 메뉴: " + menu.getMenuName() + " (활성: " + menu.getIsActive() + ")")
        );

        // When - 카테고리 삭제 (벌크 업데이트 방식)
        menuCategoryService.deleteMenuCategoryWithBulkUpdate(categoryCode, shopCode);

        // Then - 카테고리와 해당 메뉴들이 모두 비활성화되어야 함

        // 1. 해당 카테고리의 활성화된 메뉴가 없어야 함
        List<MenuDTO> activeMenusAfterDelete = menuService.getActiveMenusByCategory(categoryCode, shopCode);
        assertTrue(activeMenusAfterDelete.isEmpty(), "카테고리 삭제 후 활성화된 메뉴가 없어야 합니다.");

        // 2. 개별 메뉴들이 비활성화 상태인지 확인
        for (MenuDTO originalMenu : activeMenusBeforeDelete) {
            MenuDTO updatedMenu = menuService.getMenu(originalMenu.getMenuCode());
            assertNotNull(updatedMenu, "메뉴는 여전히 존재해야 합니다.");
            assertFalse(updatedMenu.getIsActive(), "메뉴는 비활성화 상태여야 합니다.");
            System.out.println("비활성화된 메뉴: " + updatedMenu.getMenuName() + " (활성: " + updatedMenu.getIsActive() + ")");
        }

        System.out.println("카테고리 삭제 및 연관 메뉴 비활성화 테스트 완료");
    }

    @Test
    @Order(11)
    @DisplayName("존재하지 않는 메뉴 조회 시 예외 발생 테스트")
    void testGetMenuNotFoundException() {
        // Given
        Integer nonExistentMenuCode = 99999;

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            menuService.getMenu(nonExistentMenuCode);
        });

        assertTrue(exception.getMessage().contains("메뉴를 찾을 수 없습니다"));
        System.out.println("예외 메시지: " + exception.getMessage());
    }

    @Test
    @Order(12)
    @DisplayName("존재하지 않는 카테고리로 메뉴 생성 시 예외 발생 테스트")
    void testCreateMenuCategoryNotFound() {
        // Given
        MenuDTO menuDTO = new MenuDTO();
        menuDTO.setMenuName(TEST_MENU_NAME);
        menuDTO.setMenuPrice(TEST_MENU_PRICE);
        menuDTO.setEstTime(TEST_EST_TIME);
        menuDTO.setCategoryCode(99999); // 존재하지 않는 카테고리
        menuDTO.setShopCode(TEST_SHOP_CODE);

        // When & Then
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            menuService.createMenu(menuDTO);
        });

        assertTrue(exception.getMessage().contains("메뉴 카테고리를 찾을 수 없습니다"));
        System.out.println("카테고리 없음 예외: " + exception.getMessage());
    }
}