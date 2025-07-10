package com.header.header.domain.shop.service;

import com.header.header.domain.shop.dto.ShopDTO;
import com.header.header.domain.shop.dto.ShopSummaryDTO;
import com.header.header.domain.shop.dto.ShopUpdateDTO;
import com.header.header.domain.shop.entity.Shop;
import com.header.header.domain.shop.enums.ShopStatus;
import com.header.header.domain.shop.repository.UserRepository;
import com.header.header.domain.user.entity.User;
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
    private UserRepository userRepository;

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
//        shopDTO.setShopLa(123.456);
//        shopDTO.setShopLong(123.456); //TODO. kakao api 사용하여 주소에서 위도 경도 자동으로 가져오기 .. 어케함?
        shopDTO.setShopStatus(ShopStatus.CLOSED);

        // when
        ShopDTO createdShop = shopService.createShop(shopDTO);

        //then
        assertTrue(createdShop.getShopCode() > 0);
        assertTrue(createdShop.getIsActive());
        assertEquals(createdShop.getShopName(), "기니의 빡올빡올");
    }

    @Test
    @DisplayName("READ (단건 조회)")
    void testGetShop() {
        //when
        ShopDTO foundedShop = shopService.getShopByShopCode(2);

        //then
        assertNotNull(foundedShop);
        System.out.println(foundedShop);
    }

    @Test
    @DisplayName("Shop 생성 및 상태 변경 테스트 (UPDATE - ShopUpdateDTO 사용)")
    void testCreateAndUpdateShopStatus() {
        // given - 새로운 상점 생성 (OPEN 상태로)
        ShopDTO shopDTO = new ShopDTO();
        shopDTO.setShopName("상태 테스트 상점");
        shopDTO.setCategoryCode(1);
        shopDTO.setAdminCode(testUserId);
        shopDTO.setShopLocation("서울시 송파구");
        shopDTO.setShopOpen("09:00");
        shopDTO.setShopClose("18:00");
        shopDTO.setIsActive(true);
        shopDTO.setShopPhone("010-9876-5432");
        shopDTO.setShopLa(37.5123);
        shopDTO.setShopLong(127.1023);
        shopDTO.setShopStatus(ShopStatus.OPEN);

        // when - 상점 생성
        ShopDTO createdShop = shopService.createShop(shopDTO);

        // then - 생성된 상점 검증
        assertNotNull(createdShop);
        assertEquals(ShopStatus.OPEN, createdShop.getShopStatus());

        // given - 상태 업데이트 (OPEN → CLOSE)
        Integer shopCode = createdShop.getShopCode();
        ShopUpdateDTO updateDTO = new ShopUpdateDTO();
        updateDTO.setShopName(createdShop.getShopName());
        updateDTO.setShopPhone(createdShop.getShopPhone());
        updateDTO.setShopLocation(createdShop.getShopLocation());
        updateDTO.setShopOpen(createdShop.getShopOpen());
        updateDTO.setShopClose(createdShop.getShopClose());
        updateDTO.setShopStatus(ShopStatus.CLOSED);

        // when - 상점 업데이트
        Shop updatedShop = shopService.updateShop(shopCode, updateDTO);

        // then - 업데이트된 상점 검증
        assertNotNull(updatedShop);
        assertEquals(ShopStatus.CLOSED, updatedShop.getShopStatus());

        // given - 다른 상태로 업데이트 (CLOSE → OPEN)
        updateDTO.setShopStatus(ShopStatus.OPEN);

        // when - 상점 재업데이트
        updatedShop = shopService.updateShop(shopCode, updateDTO);

        // then - 재업데이트된 상점 검증
        assertEquals(ShopStatus.OPEN, updatedShop.getShopStatus());

        // 모든 가능한 상태 테스트
        for (ShopStatus status : ShopStatus.values()) {
            updateDTO.setShopStatus(status);
            updatedShop = shopService.updateShop(shopCode, updateDTO);
            assertEquals(status, updatedShop.getShopStatus());
        }
    }

    @Test
    @DisplayName("READ (adminCode로 다수 조회 - 요약조회 DTO 사용)")
    void testGetShopsByAdminCode() {
        //when
        List<ShopSummaryDTO> foundedShopList = shopService.findShopsSummaryByAdminCode(testUserId);

        //then
        assertNotNull(foundedShopList);
        System.out.println(foundedShopList);
    }

    @Test
    @DisplayName("DELETE (deActive)")
    void testDeleteShop() {
        //given
        Integer shopCodeToDelete = 3;

        //when
        shopService.deActiveShop(shopCodeToDelete);

        //then
        ShopDTO deletedShop = shopService.getShopByShopCode(shopCodeToDelete);
        assertFalse(deletedShop.getIsActive());
    }
}