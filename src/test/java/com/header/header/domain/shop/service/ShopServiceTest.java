package com.header.header.domain.shop.service;

import com.header.header.domain.shop.dto.ShopDTO;
import com.header.header.domain.shop.enums.ShopStatus;
import com.header.header.domain.shop.exception.ShopExceptionHandler;
import com.header.header.domain.shop.projection.ShopSummary;
import com.header.header.domain.user.entity.User;
import com.header.header.domain.user.repository.MainUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ShopServiceTest {

    @Autowired
    private ShopService shopService;

    @Autowired
    private MainUserRepository userRepository;

    private Integer testUserId;

    @BeforeEach
    void setUp() {
        User testUser = userRepository.findById(1).orElseThrow();
        testUserId = testUser.getUserCode();
    }

    @Test
    @DisplayName("CREATE")
    void testCreateShop() {
        // given
        ShopDTO shopDTO = new ShopDTO();
        shopDTO.setShopName("기니의 빡올빡올");
        shopDTO.setCategoryCode(1);
        shopDTO.setAdminCode(testUserId);
        shopDTO.setShopLocation("서울특별시 종로구 세종대로 175");
        shopDTO.setShopOpen("09:00");
        shopDTO.setShopClose("18:00");
        shopDTO.setIsActive(true);
        shopDTO.setShopPhone("010-1234-5678");
        shopDTO.setShopStatus(ShopStatus.CLOSED);

        // when
        ShopDTO createdShop = shopService.createShop(shopDTO);

        //then
        assertTrue(createdShop.getShopCode() > 0);
        assertTrue(createdShop.getIsActive());
        assertEquals(createdShop.getShopLocation(), "서울특별시 종로구 세종대로 175");
        assertNotNull(createdShop.getShopLa());
        assertNotNull(createdShop.getShopLong());
        System.out.println(createdShop);
    }


    @Test
    @DisplayName("CREATE 예외: 잘못된 주소 입력")
    void testWrongAddress() {
        // given
        ShopDTO shopDTO = new ShopDTO();
        shopDTO.setShopName("기니의 빡올빡올");
        shopDTO.setCategoryCode(1);
        shopDTO.setAdminCode(testUserId);
        shopDTO.setShopLocation("내 마음은 언제나 텃밭에,,,");
        shopDTO.setShopOpen("09:00");
        shopDTO.setShopClose("18:00");
        shopDTO.setIsActive(true);
        shopDTO.setShopPhone("010-1234-5678");
        shopDTO.setShopStatus(ShopStatus.CLOSED);

        // when and then
        assertThrows(ShopExceptionHandler.class, () -> shopService.createShop(shopDTO));
    }

    @Test
    @DisplayName("READ (상세 조회)")
    void testGetShop() {
        //when
        ShopDTO foundedShop = shopService.getShopByShopCode(9);

        //then
        assertNotNull(foundedShop);
        System.out.println(foundedShop);
    }

    @Test
    @DisplayName("READ 예외: 잘못된 샵 코드")
    void testWrongShopCode() {
        //when and then
        assertThrows(ShopExceptionHandler.class, () -> shopService.getShopByShopCode(777));
    }

    @Test
    @DisplayName("READ (다수 조회)")
    void testGetShopsByAdminCode() {
        //when
        List<ShopSummary> foundedShopList = shopService.findByAdminCodeAndIsActiveTrue(testUserId);

        //then
        assertNotNull(foundedShopList);

        for (ShopSummary list: foundedShopList) {
            System.out.println("관리자 코드 : " + list.getAdminCode() + ", 활성 상태 : " + list.getIsActive() + ", 샵 이름 : " + list.getShopName());
        }
    }

    @Test
    @DisplayName("READ 예외: 잘못된 관리자 코드")
    void testWrongAdminCode() {
        //when and then
        assertThrows(ShopExceptionHandler.class, () -> shopService.findByAdminCodeAndIsActiveTrue(100));
    }

    @Test
    @DisplayName("UPDATE")
    void testCreateAndUpdateShopStatus() {
        // given - 새로운 상점 생성 (OPEN 상태로)
        ShopDTO shopDTO = new ShopDTO();
        shopDTO.setShopName("상태 테스트 상점");
        shopDTO.setCategoryCode(1);
        shopDTO.setAdminCode(testUserId);
        shopDTO.setShopLocation("서울 강남구 테헤란로 212");
        shopDTO.setShopOpen("09:00");
        shopDTO.setShopClose("18:00");
        shopDTO.setShopPhone("010-9876-5432");
        shopDTO.setShopStatus(ShopStatus.OPEN);

        // when - 상점 생성
        ShopDTO createdShop = shopService.createShop(shopDTO);

        // then - 생성된 상점 검증
        assertNotNull(createdShop);
        assertEquals(ShopStatus.OPEN, createdShop.getShopStatus());
        System.out.println(createdShop);

        // given - 상태 업데이트 (OPEN → CLOSE, 주소 변경)
        Integer shopCode = createdShop.getShopCode();
        ShopDTO updateDTO = new ShopDTO();
        updateDTO.setAdminCode(createdShop.getAdminCode());
        updateDTO.setShopName(createdShop.getShopName());
        updateDTO.setShopPhone(createdShop.getShopPhone());
        updateDTO.setCategoryCode(createdShop.getCategoryCode());
        updateDTO.setShopLocation("서울특별시 종로구 세종대로 175");
        updateDTO.setShopOpen(createdShop.getShopOpen());
        updateDTO.setShopClose(createdShop.getShopClose());
        updateDTO.setShopStatus(ShopStatus.CLOSED);

        // when - 상점 업데이트
        ShopDTO updatedShop = shopService.updateShop(shopCode, updateDTO);

        // then - 업데이트된 상점 검증
        assertNotNull(updatedShop);
        assertEquals(ShopStatus.CLOSED, updatedShop.getShopStatus());
        assertEquals("서울특별시 종로구 세종대로 175", updatedShop.getShopLocation());
    }

    @Test
    @DisplayName("UPDATE 예외: 잘못된 주소")
    void testWrongAddressWhenUpdateShop() {
        // given - 새로운 상점 생성 (OPEN 상태로)
        ShopDTO shopDTO = new ShopDTO();
        shopDTO.setShopName("상태 테스트 상점");
        shopDTO.setCategoryCode(1);
        shopDTO.setAdminCode(testUserId);
        shopDTO.setShopLocation("서울 강남구 테헤란로 212");
        shopDTO.setShopOpen("09:00");
        shopDTO.setShopClose("18:00");
        shopDTO.setShopPhone("010-9876-5432");
        shopDTO.setShopStatus(ShopStatus.OPEN);

        // when - 상점 생성
        ShopDTO createdShop = shopService.createShop(shopDTO);

        // then - 생성된 상점 검증
        assertNotNull(createdShop);
        assertEquals(ShopStatus.OPEN, createdShop.getShopStatus());
        System.out.println(createdShop);

        // given - 상태 업데이트 (OPEN → CLOSE, 잘못된 주소로 변경 시도)
        Integer shopCode = createdShop.getShopCode();
        ShopDTO updateDTO = new ShopDTO();
        updateDTO.setAdminCode(createdShop.getAdminCode());
        updateDTO.setShopName(createdShop.getShopName());
        updateDTO.setShopPhone(createdShop.getShopPhone());
        updateDTO.setCategoryCode(createdShop.getCategoryCode());
        updateDTO.setShopLocation("양상추가 파릇하지 않네요.");
        updateDTO.setShopOpen(createdShop.getShopOpen());
        updateDTO.setShopClose(createdShop.getShopClose());
        updateDTO.setShopStatus(ShopStatus.CLOSED);

        // when and then
        assertThrows(ShopExceptionHandler.class, () -> shopService.updateShop(shopCode, updateDTO));
    }


    @Test
    @DisplayName("DELETE (비활성화)")
    void testDeleteShop() {
        //given
        Integer shopCodeToDelete = 13;

        //when
        ShopDTO shopToDelete = shopService.deActiveShop(shopCodeToDelete);

        //then
        assertFalse(shopToDelete.getIsActive());
    }

    @Test
    @DisplayName("DELETE 예외: 잘못된 샵 코드")
    void testWrongShopCodeWhenDelete() {
        //given
        Integer shopCodeToDelete = 300;

        //when and then
        assertThrows(ShopExceptionHandler.class, () -> shopService.deActiveShop(shopCodeToDelete));
    }

    @Test
    @DisplayName("DELETE 예외: 재비활성화 시도")
    void testAttemptWhenDelete() {
        //given
        Integer shopCodeToDelete = 3;

        //when and then
        assertThrows(ShopExceptionHandler.class, () -> shopService.deActiveShop(shopCodeToDelete));
    }
}