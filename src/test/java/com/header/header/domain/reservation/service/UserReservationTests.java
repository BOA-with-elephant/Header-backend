package com.header.header.domain.reservation.service;

import com.header.header.auth.common.UserRole;
import com.header.header.domain.menu.entity.Menu;
import com.header.header.domain.menu.entity.MenuCategory;
import com.header.header.domain.menu.entity.MenuCategoryId;
import com.header.header.domain.menu.repository.MenuCategoryRepository;
import com.header.header.domain.menu.repository.MenuRepository;
import com.header.header.domain.reservation.dto.UserReservationDTO;
import com.header.header.domain.reservation.dto.UserReservationSearchConditionDTO;
import com.header.header.domain.reservation.entity.BossReservation;
import com.header.header.domain.reservation.enums.ReservationState;
import com.header.header.domain.reservation.exception.UserReservationExceptionHandler;
import com.header.header.domain.reservation.projection.UserReservationDetail;
import com.header.header.domain.reservation.projection.UserReservationSummary;
import com.header.header.domain.reservation.repository.BossReservationRepository;
import com.header.header.domain.reservation.repository.UserReservationRepository;
import com.header.header.domain.shop.dto.ShopDTO;
import com.header.header.domain.shop.entity.Shop;
import com.header.header.domain.shop.entity.ShopCategory;
import com.header.header.domain.shop.enums.ShopStatus;
import com.header.header.domain.shop.repository.ShopCategoryRepository;
import com.header.header.domain.shop.repository.ShopRepository;
import com.header.header.domain.user.dto.UserDTO;
import com.header.header.domain.user.entity.User;
import com.header.header.domain.user.repository.MainUserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.Date;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@SpringBootTest
@Transactional
public class UserReservationTests {

    /*테스트용 데이터 세팅용 Autowired*/
    @Autowired
    private UserReservationService userReservationService;
    @Autowired
    private ModelMapper modelMapper;
    @Autowired
    private MainUserRepository userRepository;
    @Autowired
    private ShopRepository shopRepository;
    @Autowired
    private MenuCategoryRepository menuCategoryRepository;
    @Autowired
    private MenuRepository menuRepository;
    @Autowired
    private BossReservationRepository bossReservationRepository;
    @Autowired
    private UserReservationRepository userReservationRepository;
    @Autowired
    private ShopCategoryRepository shopCategoryRepository;

    /*테스트용 데이터 세팅용 상수 - @BeforeEach 에서 초기화*/
    private Integer testUserCode;
    private Integer testShopCode;
    private Integer testMenuCategoryCode;
    private Integer testMenuCode;
    private Integer testResvCode;
    private Integer testCategoryCode;

    @BeforeEach
    @Transactional
    @Test
    void setUp() {
        /*1. 테스트용 유저*/
        UserDTO userDTO = new UserDTO();
        userDTO.setUserId("guinea");
        userDTO.setUserPwd("pass123");
        userDTO.setRole(UserRole.USER);
        userDTO.setUserName("김기니");
        userDTO.setUserPhone("010-1234-5678");
        userDTO.setIsLeave(0);

        User testUser = modelMapper.map(userDTO, User.class);
        userRepository.save(testUser);
        testUserCode = testUser.getUserCode();

        /*2. 테스트용 샵*/

        ShopCategory category;
        category = shopCategoryRepository.findById(1).orElseThrow();
        testCategoryCode = category.getCategoryCode();

        Shop testShop = Shop.builder()
                .shopName("기니샵")
                .adminInfo(testUser)
                .shopPhone("02-123-4567")
                .shopLocation("서울시 종로구")
                .shopLong(127.0276)
                .shopLa(37.2979)
                .categoryInfo(category)
                .shopOpen("09:00")
                .shopClose("18:00")
                .shopStatus(ShopStatus.OPEN)
                .isActive(true)
                .build();

        shopRepository.save(testShop);

        testShopCode = testShop.getShopCode();

        /*3. 테스트용 메뉴 카테고리 및 메뉴*/
        testMenuCategoryCode = 5; // 두피케어

        MenuCategoryId id
                = new MenuCategoryId(testMenuCategoryCode, testShopCode);

        MenuCategory menuCategory = MenuCategory.builder()
                .id(id)
                .categoryName("기니피그 전용 메뉴")
                .menuColor("#FFFFFF")
                .isActive(true)
                .build();

        menuCategoryRepository.save(menuCategory);
        testMenuCategoryCode = menuCategory.getCategoryCode();

        Menu menu = Menu.builder()
                .menuName("기니피기 손님용 특별케어")
                .menuPrice(1000)
                .estTime(300)
                .isActive(true)
                .menuCategory(menuCategory)
                .build();

        menuRepository.save(menu);
        testMenuCode = menu.getMenuCode();

        /*4. 테스트용 예약*/
        BossReservation bossReservation = BossReservation
                .builder()
                .userInfo(testUser)
                .shopInfo(testShop)
                .menuInfo(menuRepository.findById(testMenuCode).orElseThrow())
                .resvDate(new Date(2025, 7, 31))
                .resvTime(new Time(14, 0, 0))
                .userComment("푸들처럼 볶아주세요")
                .resvState(ReservationState.APPROVE)
                .build();

        bossReservationRepository.save(bossReservation);
        testResvCode = bossReservation.getResvCode();
    }

    @Test
    @Order(1)
    @DisplayName("예약 정보의 상세 조회")
    void testReadReservationDetail() {
        // when
        Optional<UserReservationDetail> result
                = userReservationService.readDetailByUserCodeAndResvCode(
                testUserCode, testResvCode
        );

        // then
        assertTrue(result.isPresent()); //Optional<>이 값을 가지고 있으면 true 리턴
        assertEquals("김기니", result.get().getUserName());
        assertEquals("기니샵", result.get().getShopName());
        System.out.println(
                result.get().getResvDate() + " / "
                + result.get().getResvTime() + " / "
                + result.get().getResvState() + " / "
                + result.get().getUserComment() + " / "
                + result.get().getShopName() + " / "
                + result.get().getShopLocation() + " / "
                + result.get().getMenuName() + " / "
                + result.get().getUserName() + " / "
                + result.get().getUserPhone()
        );
    }

    @Test
    @Order(2)
    @DisplayName("잘못된 유저 코드로 상세 조회하는 경우")
    void testReadReservationDetailWrongUserCode() {
        // when & then
        assertThrows(UserReservationExceptionHandler.class, () -> {
            userReservationService.readDetailByUserCodeAndResvCode(99999, testResvCode);
        });
    }

    @Test
    @Order(3)
    @DisplayName("잘못된 예약 코드로 상세 조회하는 경우")
    void testReadReservationDetailWrongResvCode() {
        // when & then
        assertThrows(UserReservationExceptionHandler.class, () -> {
            userReservationService.readDetailByUserCodeAndResvCode(testUserCode, 99999);
        });
    }

    @Test
    @Order(4)
    @DisplayName("자신이 예약한 내역들을 요약 조회")
    void testFindReservationSummaryByUserCode() {
        //given
        UserReservationSearchConditionDTO conditionDTO = new UserReservationSearchConditionDTO();
        conditionDTO.setUserCode(3);
        conditionDTO.setStartDate(null);
        conditionDTO.setEndDate(null);

        //when & then
        List<UserReservationSummary> results = userReservationService.findResvSummaryByUserCode(conditionDTO);

        assertNotEquals(0, results.size());
        results.forEach(r -> {
            System.out.println(
                            r.getResvDate() + " / " +
                            r.getResvTime() + " / " +
                            r.getResvState() + " / " +
                                    r.getShopName() + " / " +
                                    r.getShopLocation() + " / " +
                                            r.getMenuName()
            );
        });
    }

    @Test
    @Order(5)
    @DisplayName("잘못된 유저 코드로 요약 조회")
    void testFindReservationSummaryWrongUserCode() {
        //given
        UserReservationSearchConditionDTO conditionDTO = new UserReservationSearchConditionDTO();
        conditionDTO.setUserCode(999999);
        conditionDTO.setStartDate(null);
        conditionDTO.setEndDate(null);

        //when and then
        assertThrows(UserReservationExceptionHandler.class, () -> {
            userReservationService.findResvSummaryByUserCode(conditionDTO);
        });
    }

    @Test
    @Order(6)
    @DisplayName("탈퇴한 유저 코드로 요약 조회")
    void testFindReservationSummaryLeftUserCode() {
        //given
        UserReservationSearchConditionDTO conditionDTO = new UserReservationSearchConditionDTO();
        conditionDTO.setUserCode(5);
        conditionDTO.setStartDate(null);
        conditionDTO.setEndDate(null);

        //when and then
        assertThrows(UserReservationExceptionHandler.class, () -> {
            userReservationService.findResvSummaryByUserCode(conditionDTO);
        });
    }

    @Test
    @Order(7)
    @DisplayName("기간 필터 요약 조회")
    void testFindReservationSummaryByDate() throws ParseException {
        //given
        UserReservationSearchConditionDTO conditionDTO = new UserReservationSearchConditionDTO();
        conditionDTO.setUserCode(3);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        conditionDTO.setStartDate(Date.valueOf("2025-06-01"));
        conditionDTO.setEndDate(Date.valueOf("2025-07-31"));

        //when and then
        List<UserReservationSummary> results
                = userReservationService.findResvSummaryByUserCode(conditionDTO);

        assertNotEquals(0, results.size());

        results.forEach(r -> {
            System.out.println(
                    r.getResvDate() + " / " +
                            r.getResvTime() + " / " +
                            r.getResvState() + " / " +
                            r.getShopName() + " / " +
                            r.getShopLocation() + " / " +
                            r.getMenuName()
            );
        });
    }

    @Test
    @Order(8)
    @DisplayName("조회 시작 날짜가 조회 종료 날짜보다 이후일 경우")
    void testFindReservationSummaryWrongDate() {
        //given
        UserReservationSearchConditionDTO conditionDTO = new UserReservationSearchConditionDTO();
        conditionDTO.setUserCode(3);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        conditionDTO.setStartDate(Date.valueOf("2025-08-01"));
        conditionDTO.setEndDate(Date.valueOf("2025-07-31"));

        //when and then
        assertThrows(UserReservationExceptionHandler.class, () -> {
            userReservationService.findResvSummaryByUserCode(conditionDTO);
        });
    }

    @Test
    @Order(9)
    @DisplayName("새로운 예약을 생성 후 상세정보를 조회")
    @Transactional
    void testCreateReservation() {
        // given

        Date stringTypeDate = Date.valueOf("2025-07-31");

        UserReservationDTO dto = new UserReservationDTO();
        dto.setUserCode(testUserCode);
        dto.setShopCode(testShopCode);
        dto.setMenuCode(testMenuCode);
        dto.setResvDate(Date.valueOf("2025-07-31"));
        dto.setResvTime(Time.valueOf("14:30:00"));
        dto.setUserComment("예약 테스트입니다.");

        // when
        Optional<UserReservationDetail> result =
                userReservationService.createReservation(dto);

        // then
        assertTrue(result.isPresent());
        UserReservationDetail detail = result.get();

        assertEquals("2025-07-31", detail.getResvDate());
        assertEquals("14:30:00", detail.getResvTime());

        System.out.println(result.get().getResvDate() + " / "
                + result.get().getResvTime() + " / "
                + result.get().getResvState() + " / "
                + result.get().getShopName() + " / "
                + result.get().getShopLocation() + " / "
                + result.get().getMenuName() + " / "
                + result.get().getUserName() + " / "
                + result.get().getUserPhone()
        );
    }

    @Test
    @Order(10)
    @DisplayName("잘못된 메뉴로 예약 생성 시도")
    void testCreateReservationWrongMenu() {
        // given
        Date stringTypeDate = Date.valueOf("2025-07-31");

        UserReservationDTO dto = new UserReservationDTO();
        dto.setUserCode(testUserCode);
        dto.setShopCode(testShopCode);
        dto.setMenuCode(99999);
        dto.setResvDate(Date.valueOf("2025-07-31"));
        dto.setResvTime(Time.valueOf("14:30:00"));
        dto.setUserComment("예약 테스트입니다.");

        //when and then
        assertThrows(UserReservationExceptionHandler.class, () -> {
            userReservationService.createReservation(dto);
        });
    }

    @Test
    @Order(11)
    @DisplayName("사용자 예약 논리적 삭제")
    void testCancelReservation() {
        //when and then
        userReservationService.cancelReservation(testUserCode, testResvCode);

        BossReservation reservation = userReservationRepository.findById(testResvCode).get();

        assertNotNull(reservation);
        assertEquals(ReservationState.CANCEL, reservation.getResvState());

        System.out.println(
                reservation.getResvCode() + " / " +
                        reservation.getUserInfo().getUserName() + " / " +
                        reservation.getShopInfo().getShopName() + " / " +
                        reservation.getMenuInfo().getMenuName() + " / " +
                        reservation.getResvDate() + " / " +
                        reservation.getResvTime() + " / " +
                        reservation.getUserComment() + " / " +
                        reservation.getResvState());
    }

    @Test
    @Order(12)
    @DisplayName("잘못된 예약 코드 삭제 시도")
    void testCancelReservationWrongResvCode() {
        //when and then
        assertThrows(
                UserReservationExceptionHandler.class,
                () -> {userReservationService.cancelReservation(testUserCode, 99999);});
    }
}
