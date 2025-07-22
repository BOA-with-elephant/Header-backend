package com.header.header.domain.menu.controller;

import com.header.header.domain.menu.dto.MenuCategoryDTO;
import com.header.header.domain.menu.service.MenuCategoryService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"},
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class MenuCategoryController {

    private static final Logger log = LoggerFactory.getLogger(MenuCategoryController.class);

    private final MenuCategoryService menuCategoryService;

    public MenuCategoryController(MenuCategoryService menuCategoryService) {
        this.menuCategoryService = menuCategoryService;
    }

    /**
     * 특정 샵의 전체 메뉴 카테고리 조회
     *
     * @param shopCode 조회할 샵의 코드
     * @return 해당 샵의 모든 메뉴 카테고리 DTO 리스트
     */
    @GetMapping("/my-shops/{shopCode}/menu/category")
    public ResponseEntity<List<MenuCategoryDTO>> getAllMenuCategories(@PathVariable int shopCode) {
        List<MenuCategoryDTO> menuCategories = menuCategoryService.findAllMenuCategoriesByShopCode(
            shopCode);
        return ResponseEntity.ok(menuCategories);
    }

    /**
     * 특정 샵의 활성화된 메뉴 카테고리만 조회
     *
     * @param shopCode 조회할 샵의 코드
     * @return 해당 샵의 활성화된 메뉴 카테고리 DTO 리스트
     */
    @GetMapping("/my-shops/{shopCode}/menu/category/active")
    public ResponseEntity<List<MenuCategoryDTO>> getActiveMenuCategories(
        @PathVariable int shopCode) {
        List<MenuCategoryDTO> activeMenuCategories = menuCategoryService.findActiveMenuCategoriesByShopCode(
            shopCode);
        return ResponseEntity.ok(activeMenuCategories);
    }

    /**
     * 특정 샵의 단일 메뉴 카테고리 조회
     *
     * @param shopCode     샵 코드
     * @param categoryCode 카테고리 코드
     * @return 조회된 메뉴 카테고리 DTO
     */
    @GetMapping("/my-shops/{shopCode}/menu/category/{categoryCode}")
    public ResponseEntity<MenuCategoryDTO> getMenuCategory(@PathVariable int shopCode,
        @PathVariable int categoryCode) {
        MenuCategoryDTO menuCategory = menuCategoryService.findMenuCategoryById(categoryCode,
            shopCode);
        return ResponseEntity.ok(menuCategory);
    }

    /**
     * 메뉴 카테고리 생성
     *
     * @param shopCode        카테고리를 생성할 샵의 코드
     * @param menuCategoryDTO 생성할 카테고리 정보가 담긴 DTO
     * @return 생성된 메뉴 카테고리 DTO
     */
    @PostMapping("/my-shops/{shopCode}/menu/category")
    public ResponseEntity<MenuCategoryDTO> createMenuCategory(@PathVariable int shopCode,
        @RequestBody MenuCategoryDTO menuCategoryDTO) {
        MenuCategoryDTO createdCategory = menuCategoryService.createMenuCategory(menuCategoryDTO,
            shopCode);
        return ResponseEntity.ok(createdCategory);
    }

    /**
     * 메뉴 카테고리 수정
     *
     * @param categoryCode    수정할 카테고리 코드
     * @param shopCode        카테고리를 수정할 샵의 코드
     * @param menuCategoryDTO 수정할 카테고리 정보가 담긴 DTO
     * @return 수정된 메뉴 카테고리 DTO
     */
    @PutMapping("/my-shops/{shopCode}/menu/category/{categoryCode}")
    public ResponseEntity<MenuCategoryDTO> updateMenuCategory(
        @PathVariable int categoryCode,
        @PathVariable int shopCode,
        @RequestBody MenuCategoryDTO menuCategoryDTO) {
        MenuCategoryDTO updatedCategory = menuCategoryService.updateMenuCategory(categoryCode,
            shopCode, menuCategoryDTO);
        return ResponseEntity.ok(updatedCategory);
    }

    /**
     * 메뉴 카테고리 삭제
     *
     * @param shopCode     카테고리를 삭제할 샵의 코드
     * @param categoryCode 삭제할 카테고리 코드
     * @return 삭제 성공 메시지
     */
    @DeleteMapping("/my-shops/{shopCode}/menu/category/{categoryCode}")
    public ResponseEntity<String> deleteMenuCategory(@PathVariable int shopCode,
        @PathVariable int categoryCode) {
        menuCategoryService.deleteMenuCategoryWithBulkUpdate(categoryCode, shopCode);
        return ResponseEntity.ok("Menu category deleted successfully");
    }

}