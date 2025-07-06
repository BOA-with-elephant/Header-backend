package com.header.header.domain.menu;

import static org.junit.jupiter.api.Assertions.*;

import com.header.header.domain.menu.dto.MenuCategoryDTO;
import com.header.header.domain.menu.entity.MenuCategory;
import com.header.header.domain.menu.entity.MenuCategoryId;
import com.header.header.domain.menu.repository.MenuCategoryRepository;
import com.header.header.domain.menu.service.MenuCategoryService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
// 테스트 메서드의 실행 순서를 지정하는 어노테이션, 클래스 레벨에 선언
public class MenuCategoryRepositoryTests {

    @Autowired
    private MenuCategoryService menuCategoryService;

    @Autowired
    private MenuCategoryRepository menuCategoryRepository;

    // 테스트용 샵 코드
    private static final int TEST_SHOP_CODE = 1;

    @Test
    @Order(1)   // 각 테스트별로 실행순서를 어노테이션으로 관리
    @DisplayName("특정 샵의 메뉴카테고리 리스트 조회")
    void testFindAllMenuCategoriesByShopCode() {
        // when
        List<MenuCategoryDTO> menuCategoryList = menuCategoryService.findAllMenuCategoriesByShopCode(TEST_SHOP_CODE);

        // then
        assertNotNull(menuCategoryList);
        System.out.println("=== 샵 " + TEST_SHOP_CODE + "의 전체 메뉴 카테고리 조회 ===");
        menuCategoryList.forEach(category -> {
            assertEquals(TEST_SHOP_CODE, category.getShopCode());
            System.out.println("ShopCode: " + category.getShopCode() +
                ", CategoryCode: " + category.getCategoryCode() +
                ", CategoryName: " + category.getCategoryName() +
                ", MenuColor: " + category.getMenuColor() +
                ", IsActive: " + category.getIsActive());
        });
    }

    @Test
    @Order(2)
    @DisplayName("특정 샵의 메뉴카테고리 생성")
    // @Transactional
    void testCreateMenuCategory() {
        // given
        MenuCategoryDTO newCategory = new MenuCategoryDTO();
        newCategory.setCategoryName("테스트 카테고리");
        newCategory.setMenuColor("#FF0000");
        newCategory.setIsActive(true);

        // when
        MenuCategoryDTO createdCategory = menuCategoryService.createMenuCategory(newCategory, TEST_SHOP_CODE);

        // then
        assertNotNull(createdCategory);
        assertNotNull(createdCategory.getCategoryCode());
        assertEquals(TEST_SHOP_CODE, createdCategory.getShopCode());
        assertEquals("테스트 카테고리", createdCategory.getCategoryName());
        assertEquals("#FF0000", createdCategory.getMenuColor());
        assertTrue(createdCategory.getIsActive());

        System.out.println("=== 생성된 메뉴 카테고리 ===");
        System.out.println("ShopCode: " + createdCategory.getShopCode());
        System.out.println("CategoryCode: " + createdCategory.getCategoryCode());
        System.out.println("CategoryName: " + createdCategory.getCategoryName());
    }

    @Test
    @Order(3)
    @DisplayName("특정 샵의 메뉴카테고리 단일 조회")
    void testFindMenuCategoryById() {
        // when
        MenuCategoryDTO foundCategory = menuCategoryService.findMenuCategoryById(
            18, TEST_SHOP_CODE);

        // then
        assertNotNull(foundCategory);
        assertEquals(18, foundCategory.getCategoryCode());
        assertEquals(TEST_SHOP_CODE, foundCategory.getShopCode());
        assertEquals("테스트 카테고리", foundCategory.getCategoryName());

        System.out.println("foundCategory = " + foundCategory);
    }

    @Test
    @Order(4)
    @DisplayName("특정 샵의 메뉴카테고리 목록 조회")
    void testFindMenuCategoriesByShopCode() {
        // when
        List<MenuCategoryDTO> categoryList = menuCategoryService.findAllMenuCategoriesByShopCode(TEST_SHOP_CODE);

        // then
        assertNotNull(categoryList);
        System.out.println("=== 샵 " + TEST_SHOP_CODE + "의 메뉴 카테고리 목록 ===");
        categoryList.forEach(category -> {
            assertEquals(TEST_SHOP_CODE, category.getShopCode());
            System.out.println("CategoryCode: " + category.getCategoryCode() +
                ", CategoryName: " + category.getCategoryName());
        });
    }

    @Test
    @Order(5)
    @DisplayName("특정 샵의 활성화된 메뉴카테고리 조회")
    void testFindActiveMenuCategoriesByShopCode() {
        // when
        List<MenuCategoryDTO> activeCategories = menuCategoryService.findActiveMenuCategoriesByShopCode(TEST_SHOP_CODE);

        // then
        assertNotNull(activeCategories);
        System.out.println("=== 샵 " + TEST_SHOP_CODE + "의 활성화된 메뉴 카테고리 목록 ===");
        activeCategories.forEach(category -> {
            assertEquals(TEST_SHOP_CODE, category.getShopCode());
            assertTrue(category.getIsActive());
            System.out.println("ShopCode: " + category.getShopCode() +
                ", CategoryCode: " + category.getCategoryCode() +
                ", CategoryName: " + category.getCategoryName());
        });
    }

    @Test
    @Order(6)
    @DisplayName("특정 샵의 카테고리명으로 검색")
    void testFindMenuCategoriesByNameAndShopCode() {
        // given
        String searchKeyword = "테스트";

        // when
        List<MenuCategoryDTO> searchResults = menuCategoryService.findMenuCategoriesByNameAndShopCode(
            searchKeyword, TEST_SHOP_CODE);

        // then
        assertNotNull(searchResults);
        System.out.println("=== 샵 " + TEST_SHOP_CODE + "에서 '" + searchKeyword + "' 검색 결과 ===");
        searchResults.forEach(category -> {
            assertEquals(TEST_SHOP_CODE, category.getShopCode());
            assertTrue(category.getCategoryName().contains(searchKeyword));
            System.out.println("CategoryName: " + category.getCategoryName());
        });
    }

    @Test
    @Order(7)
    @DisplayName("특정 샵의 메뉴카테고리 수정")
    @Transactional
    void testUpdateMenuCategory() {

        // when - 카테고리 수정
        MenuCategoryDTO updateDTO = new MenuCategoryDTO();
        updateDTO.setCategoryName("수정 후 카테고리");
        updateDTO.setMenuColor("#FF00FF");
        updateDTO.setIsActive(false);

        MenuCategoryDTO updatedCategory = menuCategoryService.updateMenuCategory(
            18, TEST_SHOP_CODE, updateDTO);

        // then
        assertNotNull(updatedCategory);
        assertEquals(TEST_SHOP_CODE, updatedCategory.getShopCode());
        assertEquals("수정 후 카테고리", updatedCategory.getCategoryName());
        assertEquals("#FF00FF", updatedCategory.getMenuColor());
        assertFalse(updatedCategory.getIsActive());

        System.out.println("수정 후: " + updatedCategory.getCategoryName());
    }

    @Test
    @Order(8)
    @DisplayName("특정 샵의 메뉴카테고리 삭제 - 논리적 삭제")
    @Transactional
    void testDeleteMenuCategory() {
        // given
        int categoryCodeToDelete = 18;
        MenuCategoryId categoryId = new MenuCategoryId(categoryCodeToDelete, TEST_SHOP_CODE);

        // 삭제 전 존재 확인
        assertTrue(menuCategoryRepository.existsById(categoryId));

        // when
        menuCategoryService.deleteMenuCategory(categoryCodeToDelete, TEST_SHOP_CODE);

        // then - 논리적 삭제 성공 여부 확인
        // 1. 물리적으로는 여전히 존재해야 함
        assertTrue(menuCategoryRepository.existsById(categoryId));

        // 2. isActive가 false로 변경되었는지 확인
        MenuCategory afterDelete = menuCategoryRepository.findById(categoryId).orElse(null);
        assertNotNull(afterDelete);
        assertFalse(afterDelete.getIsActive());
    }

}