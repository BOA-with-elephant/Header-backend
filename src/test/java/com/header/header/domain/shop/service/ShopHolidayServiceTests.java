package com.header.header.domain.shop.service;

import com.header.header.domain.shop.dto.CreateHolReqDTO;
import com.header.header.domain.shop.dto.HolResDTO;
import com.header.header.domain.shop.entity.Shop;
import com.header.header.domain.shop.entity.ShopCategory;
import com.header.header.domain.shop.entity.ShopHoliday;
import com.header.header.domain.shop.repository.ShopCategoryRepository;
import com.header.header.domain.shop.repository.ShopHolidayRepository;
import com.header.header.domain.shop.repository.ShopRepository;
import com.header.header.domain.user.entity.User;
import com.header.header.domain.user.repository.MainUserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Date;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ShopHolidayServiceTests {

    @Autowired
    private ShopHolidayService shopHolidayService;
    @Autowired
    private MainUserRepository userRepository;
    @Autowired
    private ShopCategoryRepository shopCategoryRepository;
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private ShopHolidayRepository shopHolidayRepository;

    private Integer testShopCode;

    @BeforeEach
    @Test
    void setUp() {

        /*테스트용 유저*/
        User testUser = userRepository.findById(1).orElseThrow();

        /*테스트용 샵 카테고리*/
        ShopCategory category = shopCategoryRepository.findById(1).orElseThrow();

        /*테스트용 샵*/
        Shop shop = Shop.builder()
                .shopName("기니기니")
                .adminInfo(testUser)
                .shopPhone("010-1111-1111")
                .shopLocation("서울시 송파구")
                .shopLong(127.0276)
                .shopLa(37.2979)
                .categoryInfo(category)
                .shopOpen("09:00")
                .shopClose("18:00")
                .isActive(true)
                .build();

        shopRepository.save(shop);

        testShopCode = shop.getShopCode();

        /*테스트용 일시 휴무*/
        ShopHoliday tempHoliday = ShopHoliday.builder()
                .shopInfo(shop)
                .holStartDate(new Date(2025, 7, 1))
                .holEndDate(new Date(2025, 7, 10))
                .isHolRepeat(false)
                .build();
        shopHolidayRepository.save(tempHoliday);

        /*테스트용 정기 휴무 (일요일)*/
        ShopHoliday regHoliday = ShopHoliday.builder()
                .shopInfo(shop)
                .holStartDate(new Date(2025, 7, 13))
                .holEndDate(null)
                .isHolRepeat(true)
                .build();
        shopHolidayRepository.save(regHoliday);

    }

    @Test
    @Order(1)
    @DisplayName("임시 휴일 생성하기")
    void createHoliday() {
        //given
        CreateHolReqDTO dto = new CreateHolReqDTO();
        dto.setShopCode(testShopCode);
        dto.setStartDate(new Date(2025, 7, 1));
        dto.setEndDate(new Date(2025, 7, 10));
        dto.setHolRepeat(false);

        //when
        HolResDTO resDTO = shopHolidayService.createShopHoliday(dto);
        int testHolCode = resDTO.getShopHolCode();

        //then
        assertNotNull(resDTO);
        assertNotNull(testHolCode);
        System.out.println(resDTO);
        System.out.println(testHolCode);
    }
}
