package com.header.header.domain.shop.service;

import com.header.header.domain.shop.dto.ShopDTO;
import com.header.header.domain.shop.dto.ShopSummaryDTO;
import com.header.header.domain.shop.dto.ShopUpdateDTO;
import com.header.header.domain.shop.enitity.Shop;
import com.header.header.domain.shop.repository.ShopRepository;
import com.header.header.domain.shop.repository.UserRepository;
import com.header.header.domain.user.enitity.User;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
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

    /*
    * -- auto-generated definition
create table TBL_SHOP
(
    SHOP_CODE     int auto_increment
        primary key,
    CATEGORY_CODE int                  not null comment '샵 유형 코드',
    ADMIN_CODE    int                  not null comment '관리자 코드',
    SHOP_NAME     varchar(50)          not null comment '샵 이름',
    SHOP_PHONE    varchar(20)          not null comment '샵 전화번호',
    SHOP_LOCATION varchar(255)         not null comment '샵 주소',
    SHOP_LONG     decimal(10, 7)       not null comment '샵 경도',
    SHOP_LA       decimal(10, 7)       not null comment '샵 위도',
    SHOP_STATUS   varchar(20)          not null comment '샵 상태',
    SHOP_OPEN     varchar(5)           not null comment '샵 운영시간',
    SHOP_CLOSE    varchar(5)           not null comment '샵 닫는시간',
    IS_ACTIVE     tinyint(1) default 1 not null comment '활성 여부',
    constraint FK_TBL_SHOP_CATEGORY_TO_SHOP_1
        foreign key (CATEGORY_CODE) references TBL_SHOP_CATEGORY (CATEGORY_CODE),
    constraint FK_TBL_USER_TO_SHOP_1
        foreign key (ADMIN_CODE) references TBL_USER (USER_CODE)
);
    * */ //shop 테이블 ddl 정보

    @Test
    @DisplayName("CREATE")
    void testCreateShop() {
        // given
        ShopDTO shopDTO = new ShopDTO();
        shopDTO.setShopName("기니의 빡올빡올");
        shopDTO.setCategoryCode(1);
        shopDTO.setAdminCode(testUserId);
        shopDTO.setShopLocation("서울시 강남구");
        shopDTO.setShopOpen("09:00");
        shopDTO.setShopClose("18:00");
        shopDTO.setIsActive(true);
        shopDTO.setShopPhone("010-1234-5678");
        shopDTO.setShopLa(123.456);
        shopDTO.setShopLong(123.456); //TODO. kakao api 사용하여 주소에서 위도 경도 자동으로 가져오기 .. 어케함?
        shopDTO.setShopStatus("영업중");

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
        ShopDTO foundedShop = shopService.getShopByShopCode(4);

        //then
        assertNotNull(foundedShop);
        System.out.println(foundedShop);
    }


    @Test
    @DisplayName("UPDATE (ShopUpdateDTO 사용)")
    void testUpdateShopWithDTO() {
        // given
        Integer shopCodeToUpdate = 5;
        ShopDTO originalShop = shopService.getShopByShopCode(shopCodeToUpdate);

        ShopUpdateDTO updateDTO = new ShopUpdateDTO();
        updateDTO.setShopName("기니의 빠글빠글");
        updateDTO.setShopPhone(originalShop.getShopPhone());  // 기존 값 유지
        updateDTO.setShopLocation(originalShop.getShopLocation());
        updateDTO.setShopStatus("휴업");  // 상태 변경
        updateDTO.setShopOpen("10:00");  // 오픈 시간 변경
        updateDTO.setShopClose(originalShop.getShopClose());

        // when
        Shop updatedShop = shopService.updateShop(shopCodeToUpdate, updateDTO);

        // then
        assertNotNull(updatedShop);
        assertEquals("기니의 빠글빠글", updatedShop.getShopName());
        assertEquals("휴업", updatedShop.getShopStatus());
        assertEquals("10:00", updatedShop.getShopOpen());
        assertEquals(shopCodeToUpdate, updatedShop.getShopCode());

        // 변경하지 않은 필드
        assertEquals(originalShop.getAdminCode(), updatedShop.getAdminCode());
        assertEquals(originalShop.getCategoryCode(), updatedShop.getCategoryCode());
        assertEquals(originalShop.getIsActive(), updatedShop.getIsActive());
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
        Integer shopCodeToDelete = 4;

        //when
        shopService.deActiveShop(shopCodeToDelete);

        //then
        ShopDTO deletedShop = shopService.getShopByShopCode(shopCodeToDelete);
        assertFalse(deletedShop.getIsActive());
    }
}