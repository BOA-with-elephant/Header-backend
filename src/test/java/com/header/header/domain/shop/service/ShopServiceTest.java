package com.header.header.domain.shop.service;

import com.header.header.domain.shop.dto.ShopCreationDTO;
import com.header.header.domain.shop.dto.ShopDTO;
import com.header.header.domain.shop.dto.ShopUpdateDTO;
import com.header.header.domain.shop.entity.Shop;
import com.header.header.domain.shop.entity.ShopCategory;
import com.header.header.domain.shop.exception.ShopExceptionHandler;
import com.header.header.domain.shop.projection.ShopDetailResponse;
import com.header.header.domain.shop.projection.ShopSearchSummaryResponse;
import com.header.header.domain.shop.projection.ShopSummary;
import com.header.header.domain.shop.repository.ShopCategoryRepository;
import com.header.header.domain.shop.repository.ShopRepository;
import com.header.header.domain.user.entity.User;
import com.header.header.domain.user.repository.MainUserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Commit;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) //테스트 코드 순서 지정
class ShopServiceTest {

    @Autowired
    private ShopService shopService;

    @Autowired
    private MainUserRepository userRepository;

    @Autowired
    private ShopCategoryRepository shopCategoryRepository;

    @Autowired
    private ShopRepository shopRepository;

    private static Integer testShopCode;
    private static Integer testCategoryCode;
    private Integer testUserId;


    /*테스트용 데이터 세팅*/
    @BeforeEach
    @Test
    void setUp() {

        ShopCategory category;
        category = shopCategoryRepository.findById(1).orElseThrow();
        testCategoryCode = category.getCategoryCode();

        User testUser = userRepository.findById(1).orElseThrow();
        testUserId = testUser.getUserCode();

        Shop shop1 = Shop.builder()
                .shopName("가까우미")
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
        shopRepository.save(shop1);

        testShopCode = shop1.getShopCode();

        System.out.println(shop1.getShopLocation() + "/" + shop1.getShopLa() + "/" + shop1.getShopLong()
        + " / " + shop1.getShopName() + "/" + shop1.getShopPhone() + "/" + shop1.getShopOpen() + "/" + shop1.getShopClose());

        Shop shop2 = Shop.builder()
                .shopName("멀리 있는 샵")
                .adminInfo(testUser)
                .shopPhone("010-2222-2222")
                .shopLocation("부산광역시")
                .shopLong(129.0756)
                .shopLa(35.1796)
                .categoryInfo(category)
                .shopOpen("09:00")
                .shopClose("18:00")
                .isActive(true)
                .build();
        shopRepository.save(shop2);
    }

    @Test
    @Order(1)
    @DisplayName("샵 생성 테스트")
    void testCreateShopUsingDTO() {
        ShopCreationDTO dto = new ShopCreationDTO();
        dto.setCategoryCode(testCategoryCode);
        dto.setAdminCode(testUserId);
        dto.setShopName("테스트 샵");
        dto.setShopPhone("010-1234-5678");
        dto.setShopOpen("09:00");
        dto.setShopClose("18:00");

        dto.setShopLocation("서울특별시 종로구 세종대로 175");

        ShopDTO createdShop = shopService.createShop(dto);

        testShopCode = createdShop.getShopCode();
        assertNotNull(testShopCode);
        System.out.println(createdShop);
    }

    @Test
    @Order(2)
    @DisplayName("샵 리스트 거리순 정렬, 페이징 처리 확인")
    void testSearchShopByLocation() {
        // 기준 위치: 서울 강남 (사용자 위치)
        double userLat = 37.4979;
        double userLong = 127.0276;
        int categoryCode = testCategoryCode;  // 실제 저장된 카테고리 코드 사용

        Pageable pageable = PageRequest.of(0, 10);

        Page<ShopSearchSummaryResponse> result = shopService.findShopsByCondition(
                userLat, userLong, categoryCode, null, pageable
        );

        List<ShopSearchSummaryResponse> content = result.getContent();

        content.forEach(dto -> {
            System.out.println(
                    dto.getShopName() + " / " + dto.getDistance() + "m"
            );
        });
    }

    @Test
    @Order(3)
    @DisplayName("샵 이름 키워드 검색 테스트")
    void testSearchShopsByLocationAndKeyword() {
        double userLat = 37.4979;
        double userLong = 127.0276;
        int categoryCode = testCategoryCode;

        Pageable pageable = PageRequest.of(0, 10);
        String keyword = "멀리";

        Page<ShopSearchSummaryResponse> result = shopService.findShopsByCondition(
                userLat, userLong, categoryCode, keyword, pageable
        );

        List<ShopSearchSummaryResponse> content = result.getContent();

        content.forEach(dto -> {
            System.out.println(
                    dto.getShopName() + " / " + dto.getDistance() + "m"
            );
        });

        assertNotNull(content);
    }

    @Test
    @Order(4)
    @DisplayName("사용자가 위치 권한을 허용을 하지 않았을 경우")
    void testSearchShopWithoutLocation() {

        int categoryCode = testCategoryCode;  // 실제 저장된 카테고리 코드 사용

        Pageable pageable = PageRequest.of(0, 10);

        Page<ShopSearchSummaryResponse> result = shopService.findShopsByCondition(
                null, null, categoryCode, null, pageable
        );

        List<ShopSearchSummaryResponse> content = result.getContent();

        content.forEach(dto -> {
            System.out.println(
                    dto.getShopName() + " / " + dto.getDistance() + "m"
            );
        });

        assertNotNull(content);
    }

    @Test
    @Order(5)
    @DisplayName("관리자가 본인 샵 리스트 조회 (요약 정보)")
    void testSearchShopsByAdminCode() {
        List<ShopSummary> shopSummaries =  shopService.readShopSummaryByAdminCode(testUserId);

        assertNotNull(shopSummaries);

        shopSummaries.forEach(list -> System.out.println(
                list.getShopName() + " / " + list.getCategoryName() + " / " +
                list.getShopLocation() + "/" + list.getCategoryName()));
    }

    @Test
    @Order(6)
    @DisplayName("사용자 혹은 관리자가 샵을 상세 조회하는 경우")
    void testGetShopDetail() {
        //when
        List<ShopDetailResponse> result = shopService.readShopDetailByShopCode(1);
        result.forEach(dto -> System.out.println(
                dto.getShopName() + " / " +
                        dto.getCategoryName() + " / " +
                        dto.getShopPhone() + " / " +
                        dto.getShopLocation() + " / " +
                        dto.getShopOpen() + " / " +
                        dto.getShopClose() + " / " +
                        dto.getMenuCategoryName() + " / " +
                        dto.getMenuName()+ " / " +
                        dto.getMenuPrice()+ " / " +
                        dto.getEstTime()
        ));
    }

    @Test
    @Order(7)
    @DisplayName("관리자가 샵의 주소를 수정하지 않는 경우")
    void testUpdateShop() {
        // given
        ShopUpdateDTO dto = new ShopUpdateDTO();
        dto.setShopName("변경된 샵 이름");
        dto.setShopPhone("010-9999-9999");
        dto.setShopLocation("서울시 송파구"); // 기존과 동일
        dto.setShopOpen("10:00");
        dto.setShopClose("19:00");
        dto.setCategoryCode(testCategoryCode); // 기존과 동일

        // when
        ShopDTO updated = shopService.updateShop(testUserId, testShopCode, dto);

        // then
        assertEquals("변경된 샵 이름", updated.getShopName());
        assertEquals("010-9999-9999", updated.getShopPhone());
        assertEquals("10:00", updated.getShopOpen());
        assertEquals("19:00", updated.getShopClose());

        // 위도/경도는 변경 안됨
        Shop shop = shopRepository.findById(testShopCode).orElseThrow();
        assertEquals(127.0276, shop.getShopLong());
        assertEquals(37.2979, shop.getShopLa());
        System.out.println(updated);
        System.out.println(shop.getShopLocation() + "/" + shop.getShopLa() + "/" + shop.getShopLong()
        +"/" + shop.getShopName() + "/" + shop.getShopPhone() + "/" + shop.getShopOpen() + "/" + shop.getShopClose());
    }

    @Test
    @Order(8)
    @DisplayName("관리자가 샵의 주소를 수정하는 경우")
    void testUpdateShopWithLocation() {
        // given
        ShopUpdateDTO dto = new ShopUpdateDTO();
        dto.setShopLocation("서울 강남구 테헤란로 212"); // 다른 주소로 변경
        dto.setShopName("위치 바뀐 샵");
        dto.setShopPhone("010-1234-5678");
        dto.setShopOpen("10:30");
        dto.setShopClose("20:00");
        dto.setCategoryCode(testCategoryCode);

        ShopDTO updated = shopService.updateShop(testUserId, testShopCode, dto);

        System.out.println(updated);

        Shop shop = shopRepository.findById(testShopCode).orElseThrow();
        assertNotEquals(127.0276, shop.getShopLong());
        assertNotEquals(37.2979, shop.getShopLa());
        System.out.println(updated);
        System.out.println(shop.getShopLocation() + "/" + shop.getShopLa() + "/" + shop.getShopLong()
                +"/" + shop.getShopName() + "/" + shop.getShopPhone() + "/" + shop.getShopOpen() + "/" + shop.getShopClose());
    }

    @Test
    @Order(9)
    @DisplayName("DELETE (비활성화)")
    @Commit
    void testDeleteShop() {
        //given
        Integer shopCodeToDelete =  testShopCode;
        System.out.println("shopCodeToDelete : " + shopCodeToDelete);

        //when
        shopService.deActiveShop(testUserId, shopCodeToDelete);
        Shop shop = shopRepository.findById(shopCodeToDelete).orElseThrow();

        //then
        assertFalse(shop.getIsActive());
    }

    @Test
    @Order(10)
    @DisplayName("DELETE 예외: 잘못된 샵 코드")
    void testWrongShopCodeWhenDelete() {
        //given
        Integer shopCodeToDelete = testShopCode + 1000000000;

        //when and then
        assertThrows(ShopExceptionHandler.class, () -> shopService.deActiveShop(testUserId, shopCodeToDelete));
    }

    @Test
    @Order(11)
    @DisplayName("DELETE 예외: 재비활성화 시도")
    void testAttemptWhenDelete() {
        //given
        Integer shopCodeToDelete = 114;
        System.out.println("shopCodeToDelete : " + shopCodeToDelete);

        //when and then
        assertThrows(ShopExceptionHandler.class, () -> shopService.deActiveShop(testUserId, shopCodeToDelete));
    }
}