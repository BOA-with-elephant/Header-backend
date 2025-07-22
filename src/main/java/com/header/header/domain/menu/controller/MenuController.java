package com.header.header.domain.menu.controller;

import com.header.header.domain.menu.dto.MenuDTO;
import com.header.header.domain.menu.service.MenuService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"},
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class MenuController {

    private static final Logger log = LoggerFactory.getLogger(MenuController.class);

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * 특정 샵의 전체 메뉴 조회
     * @param shopCode 조회할 샵의 코드
     * @return 해당 샵의 모든 메뉴 DTO 리스트
     */
    @GetMapping("/my-shops/{shopCode}/menu")
    public ResponseEntity<List<MenuDTO>> getAllMenus(@PathVariable Integer shopCode) {
        try {
            log.debug("샵코드 {}의 메뉴 전체 조회 요청", shopCode);

            List<MenuDTO> menus = menuService.getMenusByShop(shopCode);

            log.debug("샵코드 {}의 메뉴 조회 완료. 조회된 개수: {}", shopCode, menus.size());

            return ResponseEntity.ok(menus);

        } catch (Exception e) {
            log.error("샵코드 {}의 메뉴 조회 중 오류 발생", shopCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 샵의 활성화된 메뉴만 조회
     * @param shopCode 조회할 샵의 코드
     * @return 해당 샵의 활성화된 메뉴 DTO 리스트
     */
    @GetMapping("/my-shops/{shopCode}/menu/active")
    public ResponseEntity<List<MenuDTO>> getActiveMenus(@PathVariable Integer shopCode) {
        try {
            log.debug("샵코드 {}의 활성화된 메뉴 조회 요청", shopCode);

            List<MenuDTO> activeMenus = menuService.getActiveMenusByShop(shopCode);

            log.debug("샵코드 {}의 활성화된 메뉴 조회 완료. 조회된 개수: {}", shopCode, activeMenus.size());

            return ResponseEntity.ok(activeMenus);

        } catch (Exception e) {
            log.error("샵코드 {}의 활성화된 메뉴 조회 중 오류 발생", shopCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 카테고리의 메뉴 조회
     * @param shopCode 샵 코드
     * @param categoryCode 카테고리 코드
     * @return 해당 카테고리의 메뉴 DTO 리스트
     */
    @GetMapping("/my-shops/{shopCode}/menu/by-category/{categoryCode}")
    public ResponseEntity<List<MenuDTO>> getMenusByCategory(@PathVariable Integer shopCode, @PathVariable Integer categoryCode) {
        try {
            log.debug("샵코드 {}, 카테고리코드 {}의 메뉴 조회 요청", shopCode, categoryCode);

            List<MenuDTO> menus = menuService.getActiveMenusByCategory(categoryCode, shopCode);

            log.debug("샵코드 {}, 카테고리코드 {}의 메뉴 조회 완료. 조회된 개수: {}", shopCode, categoryCode, menus.size());

            return ResponseEntity.ok(menus);

        } catch (Exception e) {
            log.error("샵코드 {}, 카테고리코드 {}의 메뉴 조회 중 오류 발생", shopCode, categoryCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 샵의 단일 메뉴 조회
     * @param shopCode 샵 코드
     * @param menuCode 메뉴 코드
     * @return 조회된 메뉴 DTO
     */
    @GetMapping("/my-shops/{shopCode}/menu/{menuCode}")
    public ResponseEntity<MenuDTO> getMenu(@PathVariable Integer shopCode, @PathVariable Integer menuCode) {
        try {
            log.debug("샵코드 {}, 메뉴코드 {}의 메뉴 조회 요청", shopCode, menuCode);

            MenuDTO menu = menuService.getMenu(menuCode);

            log.debug("샵코드 {}, 메뉴코드 {}의 메뉴 조회 완료", shopCode, menuCode);

            return ResponseEntity.ok(menu);

        } catch (Exception e) {
            log.error("샵코드 {}, 메뉴코드 {}의 메뉴 조회 중 오류 발생", shopCode, menuCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 메뉴명으로 검색
     * @param shopCode 샵 코드
     * @param menuName 검색할 메뉴명
     * @return 검색된 메뉴 DTO 리스트
     */
    @GetMapping("/my-shops/{shopCode}/menu/search")
    public ResponseEntity<List<MenuDTO>> searchMenusByName(@PathVariable Integer shopCode, @RequestParam String menuName) {
        try {
            log.debug("샵코드 {}에서 메뉴명 '{}'로 검색 요청", shopCode, menuName);

            List<MenuDTO> menus = menuService.searchMenusByName(menuName, shopCode);

            log.debug("샵코드 {}에서 메뉴명 '{}'로 검색 완료. 조회된 개수: {}", shopCode, menuName, menus.size());

            return ResponseEntity.ok(menus);

        } catch (Exception e) {
            log.error("샵코드 {}에서 메뉴명 '{}'로 검색 중 오류 발생", shopCode, menuName, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 메뉴 생성
     * @param shopCode 메뉴를 생성할 샵의 코드
     * @param menuDTO 생성할 메뉴 정보가 담긴 DTO
     * @return 생성된 메뉴 DTO
     */
    @PostMapping("/my-shops/{shopCode}/menu")
    public ResponseEntity<MenuDTO> createMenu(@PathVariable Integer shopCode, @RequestBody MenuDTO menuDTO) {
        try {
            log.debug("샵코드 {}에 메뉴 생성 요청: {}", shopCode, menuDTO.getMenuName());

            MenuDTO createdMenu = menuService.createMenu(menuDTO);

            log.info("샵코드 {}에 메뉴 생성 완료: 메뉴코드 {}, 메뉴명 '{}'",
                shopCode, createdMenu.getMenuCode(), createdMenu.getMenuName());

            return ResponseEntity.ok(createdMenu);

        } catch (Exception e) {
            log.error("샵코드 {}에 메뉴 생성 중 오류 발생", shopCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 메뉴 수정
     * @param shopCode 수정할 메뉴가 속한 샵 코드
     * @param menuCode 수정할 메뉴 코드
     * @param menuDTO 수정할 정보가 담긴 DTO
     * @return 수정된 메뉴 DTO
     */
    @PutMapping("/my-shops/{shopCode}/menu/{menuCode}")
    public ResponseEntity<MenuDTO> updateMenu(@PathVariable Integer shopCode, @PathVariable Integer menuCode, @RequestBody MenuDTO menuDTO) {
        try {
            log.debug("샵코드 {}, 메뉴코드 {}의 메뉴 수정 요청", shopCode, menuCode);

            MenuDTO updatedMenu = menuService.updateMenu(menuCode, menuDTO);

            log.info("샵코드 {}, 메뉴코드 {}의 메뉴 수정 완료", shopCode, menuCode);

            return ResponseEntity.ok(updatedMenu);

        } catch (Exception e) {
            log.error("샵코드 {}, 메뉴코드 {}의 메뉴 수정 중 오류 발생", shopCode, menuCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 메뉴 삭제 (논리적 삭제)
     * @param shopCode 삭제할 메뉴가 속한 샵 코드
     * @param menuCode 삭제할 메뉴 코드
     * @return 삭제 완료 응답
     */
    @DeleteMapping("/my-shops/{shopCode}/menu/{menuCode}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Integer shopCode, @PathVariable Integer menuCode) {
        try {
            log.debug("샵코드 {}, 메뉴코드 {}의 메뉴 삭제 요청", shopCode, menuCode);

            menuService.deleteMenu(menuCode);

            log.info("샵코드 {}, 메뉴코드 {}의 메뉴 삭제 완료", shopCode, menuCode);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("샵코드 {}, 메뉴코드 {}의 메뉴 삭제 중 오류 발생", shopCode, menuCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}