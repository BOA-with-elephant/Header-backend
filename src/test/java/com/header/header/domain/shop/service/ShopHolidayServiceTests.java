package com.header.header.domain.shop.service;

import com.header.header.domain.menu.entity.Menu;
import com.header.header.domain.menu.entity.MenuCategory;
import com.header.header.domain.menu.entity.MenuCategoryId;
import com.header.header.domain.menu.repository.MenuCategoryRepository;
import com.header.header.domain.menu.repository.MenuRepository;
import com.header.header.domain.reservation.repository.UserReservationRepository;
import com.header.header.domain.shop.dto.HolCreationDTO;
import com.header.header.domain.shop.dto.HolResDTO;
import com.header.header.domain.shop.dto.HolUpdateDTO;
import com.header.header.domain.shop.entity.Shop;
import com.header.header.domain.shop.entity.ShopCategory;
import com.header.header.domain.shop.entity.ShopHoliday;
import com.header.header.domain.shop.exception.ShopHolidayExceptionHandler;
import com.header.header.domain.shop.projection.ShopHolidayInfo;
import com.header.header.domain.shop.repository.ShopCategoryRepository;
import com.header.header.domain.shop.repository.ShopHolidayRepository;
import com.header.header.domain.shop.repository.ShopRepository;
import com.header.header.domain.user.entity.User;
import com.header.header.domain.user.repository.MainUserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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
    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private UserReservationRepository userReservationRepository;
    @Autowired
    private MenuCategoryRepository menuCategoryRepository;

    private Integer testShopCode;
    private Integer testMenuCategoryCode;
    private Integer testMenuCode;

    @Commit
    void setUp() {

        /*테스트용 유저*/
        User testUser = userRepository.findById(1).orElseThrow();

        /*테스트용 샵 카테고리*/
        ShopCategory category = shopCategoryRepository.findById(1).orElseThrow();

        /*테스트용 샵*/
        Shop testShop = Shop.builder()
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

        shopRepository.save(testShop);

        testShopCode = testShop.getShopCode();

        /*테스트용 일시 휴무*/
        ShopHoliday tempHoliday = ShopHoliday.builder()
                .shopInfo(testShop)
                .holStartDate(new Date(2025, 7, 1))
                .holEndDate(new Date(2025, 7, 10))
                .isHolRepeat(false)
                .build();
        shopHolidayRepository.save(tempHoliday);

        /*테스트용 정기 휴무 (일요일)*/
        ShopHoliday regHoliday = ShopHoliday.builder()
                .shopInfo(testShop)
                .holStartDate(new Date(2025, 7, 13))
                .holEndDate(null)
                .isHolRepeat(true)
                .build();
        shopHolidayRepository.save(regHoliday);

        /*테스트용 메뉴 카테고리 및 메뉴*/
        testMenuCategoryCode = 5; // 두피케어

        MenuCategoryId id
                = new MenuCategoryId(testMenuCategoryCode, testShopCode);

        // 수정된 코드
        MenuCategory menuCategory = MenuCategory.builder()
                .id(id)
                .categoryName("기니피그 전용 메뉴")
                .menuColor("#FFFFFF")
                .isActive(true)
                .build();
        MenuCategory savedMenuCategory = menuCategoryRepository.save(menuCategory);

        // 또는 명시적으로 재조회
        MenuCategory managedMenuCategory = menuCategoryRepository.findById(id).orElseThrow();

        Menu menu = Menu.builder()
                .menuName("기니피기 손님용 특별케어")
                .menuPrice(1000)
                .estTime(300)
                .isActive(true)
                .menuCategory(managedMenuCategory)  // managed 상태의 엔티티 사용
                .build();
        Menu testMenu = menuRepository.save(menu);
        Menu managedMenu = menuRepository.findById(testMenu.getMenuCode()).orElseThrow();

/*        // 테스트용 예약
        BossReservation reservation = BossReservation
                .builder()
                .userInfo(testUser)
                .shopInfo(testShop)
                .menuInfo(managedMenu)
                .resvDate(new Date(2025, 8, 12)) //8월 화요일 = 화요일 시도하면 예외, 8월 12일 포함된 날짜 일시휴무 생성 시도하면 예외
                .resvTime(new Time(14, 0, 0))
                .userComment("ttt")
                .resvState(ReservationState.APPROVE)
                .build();

        userReservationRepository.save(reservation);*/

    }

    private final Integer SHOP_CODE = 1;
    private final Integer HOL_CODE = 1;

    @Test
    @Order(1)
    @DisplayName("임시 휴일 생성하기")
    void createHoliday() {
        //given
        HolCreationDTO dto = new HolCreationDTO();
        dto.setStartDate(new Date(2025, 7, 1));
        dto.setEndDate(new Date(2025, 7, 10));
        dto.setIsHolRepeat(false);

        //when
        HolResDTO resDTO = shopHolidayService.createShopHoliday(1, testShopCode, dto);
        int testHolCode = resDTO.getShopHolCode();

        //then
        assertNotNull(resDTO);
        assertNotNull(testHolCode);
        System.out.println(resDTO);
        System.out.println(testHolCode);
    }

    // 예약: 8/30 (토) 샵코드 1에 대해 있음
    // Date modify :Date.valueOf("2025-09-30") format

    @Test
    @Order(2)
    @DisplayName("예약 있는 날짜에 임시 휴일 생성 시도")
    void createHolidayInReservedDate() {
        //given (반복하지 않는 일시 휴일, 8/10 ~ 8/30)
        HolCreationDTO dto = new HolCreationDTO();
        dto.setStartDate(Date.valueOf(LocalDate.of(2025, 8, 10)));
        dto.setEndDate(Date.valueOf(LocalDate.of(2025, 8, 30)));
        dto.setIsHolRepeat(false);

        //when and then
        assertThrows(ShopHolidayExceptionHandler.class,
                () -> shopHolidayService.createShopHoliday(1, testShopCode, dto));
    }

    @Test
    @Order(3)
    @DisplayName("예약 있는 날짜에 정기 휴일 생성 시도")
    void createRegHolidayInReservedDate() {
        //given
        HolCreationDTO dto = new HolCreationDTO();
        dto.setStartDate(Date.valueOf(LocalDate.of(2025, 8, 2)));
        dto.setEndDate(null);
        dto.setIsHolRepeat(true);

        //when and then
        assertThrows(ShopHolidayExceptionHandler.class,
                () -> shopHolidayService.createShopHoliday(1, testShopCode, dto));
    }

    @Test
    @Order(4)
    @DisplayName("휴일 수정하기")
    void testSuccessModify() {
        // given
        HolUpdateDTO dto = HolUpdateDTO.builder()
                .startDate(Date.valueOf("2025-09-30"))
                .endDate(Date.valueOf("2025-10-30"))
                .isHolRepeat(false)
                .build();

        //when and then
        HolResDTO resDto = shopHolidayService.updateShopHoliday(1, SHOP_CODE, HOL_CODE, dto);
        assertNotNull(resDto);
        System.out.println(resDto);
    }

    // exception - reg, temp test
    // read (whole, and specify ???, projection... easy)
    // delete (physical) - easy...

    @Test
    @Order(5)
    @DisplayName("예약 있는 날짜에 정기 휴일 변경 시도")
    void testModifyWhenReservedDate() {
        //given
        HolUpdateDTO dto = HolUpdateDTO.builder()
                .startDate(Date.valueOf("2025-08-02")) // 화요일
                .endDate(null)
                .isHolRepeat(true)
                .build();

        //when and then
        assertThrows(ShopHolidayExceptionHandler.class,
                () -> shopHolidayService.updateShopHoliday(1, SHOP_CODE, HOL_CODE, dto));
    }

    @Test
    @Order(6)
    @DisplayName("예약 있는 날짜에 일시 휴일 변경 시도")
    void testModifyTempWhenReservedDate() {
        //given
        HolUpdateDTO dto = HolUpdateDTO.builder()
                .startDate(Date.valueOf("2025-08-30"))
                .endDate(Date.valueOf("2025-09-01"))
                .isHolRepeat(false)
                .build();

        //when and then
        assertThrows(ShopHolidayExceptionHandler.class,
                () -> shopHolidayService.updateShopHoliday(1, SHOP_CODE, HOL_CODE, dto));
    }

    @Test
    @Order(7)
    @DisplayName("샵이 가진 휴일 정보 불러오기")
    void testReadShopHoliday() {
        //given
        Integer shopCode = 1;

        //when and then
        List<ShopHolidayInfo> hols
                = shopHolidayService.getShopHolidayInfo(1, shopCode);

        assertNotNull(hols);
        hols.forEach(hol -> System.out.println(
                hol.getHolStartDate() + " ~ " + hol.getHolEndDate()
                + " / " + hol.getIsHolRepeat()
        ));
    }

    @Test
    @Order(8)
    @DisplayName("휴일 정보 삭제 테스트")
    @Transactional
    void testDeleteHoliday() {
        //given
        ShopHoliday tempHoliday = ShopHoliday.builder()
                .shopInfo(shopRepository.findById(SHOP_CODE).orElseThrow())
                .holStartDate(new Date(2025, 7, 1))
                .holEndDate(new Date(2025, 7, 10))
                .isHolRepeat(false)
                .build();
        shopHolidayRepository.save(tempHoliday);
        Integer shopHolCodeToDelete = tempHoliday.getShopHolCode();

        //when and then
        shopHolidayService.deleteShopHoliday(1,  SHOP_CODE, shopHolCodeToDelete);
        assertTrue(shopHolidayRepository.findById(shopHolCodeToDelete).isEmpty());
    }

    @Test
    @Order(9)
    @DisplayName("해당 샵이 가지지 않은 휴일 정보 삭제 시도")
    @Transactional
    void testDeleteWrongHoliday() {
        //given
        ShopHoliday tempHoliday = ShopHoliday.builder()
                .shopInfo(shopRepository.findById(SHOP_CODE).orElseThrow())
                .holStartDate(new Date(2025, 7, 1))
                .holEndDate(new Date(2025, 7, 10))
                .isHolRepeat(false)
                .build();
        shopHolidayRepository.save(tempHoliday);
        Integer shopHolCodeToDelete = tempHoliday.getShopHolCode();

        //when and then
        assertThrows(ShopHolidayExceptionHandler.class,
                () -> shopHolidayService.deleteShopHoliday(1, SHOP_CODE, 100000));
    }
}
