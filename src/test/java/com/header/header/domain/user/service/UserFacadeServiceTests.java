package com.header.header.domain.user.service;

import com.header.header.auth.common.ApiResponse;
import com.header.header.auth.exception.DuplicatedPhoneException;
import com.header.header.auth.exception.DuplicatedUserIdException;
import com.header.header.auth.exception.RegistrationUnknownException;
import com.header.header.auth.model.AuthDetails;
import com.header.header.auth.model.dto.LoginUserDTO;
import com.header.header.domain.shop.dto.ShopCreationDTO;
import com.header.header.domain.shop.dto.ShopDTO;
import com.header.header.domain.shop.service.ShopService;
import com.header.header.domain.user.dto.UserDTO;
import com.header.header.domain.user.entity.User;
import com.header.header.domain.user.repository.MainUserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.security.auth.login.FailedLoginException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ComponentScan(basePackages = "com.header.header")
public class UserFacadeServiceTests {
    @Autowired
    private ShopService shopService;

    @Autowired
    private UserFacadeService facadeService;

    @Autowired
    private MainUserRepository userRepository; // User 엔티티의 상태를 읽기 위해 사용

    @Test
    @DisplayName("회원가입(user 생성) test")
    void registerUserTest() {
        // given1. 기존 정보와 중복 없이 가입할 때 사용할 UserDTO 생성
        UserDTO newSignup = new UserDTO();
        newSignup.setUserId("user105");
        newSignup.setUserPwd("pwd105");
        newSignup.setUserName("ElleFenning");
        newSignup.setUserPhone("010-1289-2378");
        newSignup.setBirthday(LocalDate.parse("1999-12-30"));

        // when: 회원가입 서비스 호출
        UserDTO result1 = facadeService.registerUser(newSignup);

        // then: 회원가입 성공 여부 및 반환된 UserDTO의 유효성 검증
        assertNotNull(result1);
        System.out.println("가입 성공 정보: " + result1);
        //가입 성공 정보: UserDTO(userCode=0, userId=user105, userPwd=pwd105, isAdmin=0, userName=ElleFenning, userPhone=010-1289-2378, birthday=1999-12-30, isLeave=0)
    }

    @Test
    @DisplayName("회원가입 시 중복 체크 테스트1")
    void registerUserTest2() {
        // given2. 중복된 userId로 가입 시도할 UserDTO 생성
        UserDTO duplicateIdDTO = new UserDTO();
        duplicateIdDTO.setUserId("kimtaehee"); //db에 있는 Id
        duplicateIdDTO.setUserPwd("test1234");
        duplicateIdDTO.setUserName("Test User");
        duplicateIdDTO.setUserPhone("010-9999-9999");
        duplicateIdDTO.setBirthday(LocalDate.parse("2025-07-21"));

        // when/then: 중복 아이디로 가입 시도 시 DuplicatedUserIdException 발생 확인
        DuplicatedUserIdException thrownIdException = assertThrows(
                DuplicatedUserIdException.class,
                () -> facadeService.registerUser(duplicateIdDTO),
                "중복된 아이디로 회원가입 시 DuplicatedUserIdException이 발생해야 합니다."
        );
        assertEquals("이미 존재하는 아이디입니다.", thrownIdException.getMessage());
        System.out.println("중복 아이디 에러 메시지: " + thrownIdException.getMessage());
    }

    @Test
    @DisplayName("회원가입 시 중복 체크 테스트2")
    void registerUserTest3() {
        // given3. 중복된 전화번호로 가입 시도할 UserDTO 생성
        UserDTO duplicatePhoneDTO = new UserDTO();
        duplicatePhoneDTO.setUserId("newuser01");
        duplicatePhoneDTO.setUserPwd("pass001");
        duplicatePhoneDTO.setUserName("Jane Doe");
        duplicatePhoneDTO.setUserPhone("010-1011-1011"); // db에 있는 전화번호와 동일
        duplicatePhoneDTO.setBirthday(LocalDate.parse("2025-07-10"));

        // when/then: 중복 전화번호로 가입 시도 시 DuplicatedPhoneException 발생 확인
        DuplicatedPhoneException thrownPhoneException = assertThrows(
                DuplicatedPhoneException.class,
                () -> facadeService.registerUser(duplicatePhoneDTO),
                "중복된 전화번호로 회원가입 시 DuplicatedPhoneException이 발생해야 합니다."
        );
        assertEquals("이미 존재하는 전화번호입니다.", thrownPhoneException.getMessage(), "중복 전화번호 에러 메시지가 일치해야 합니다.");
        System.out.println("중복 전화번호 에러 메시지: " + thrownPhoneException.getMessage());
    }

    @Test
    @DisplayName("Read 로그인 테스트")
    void facadeLoginTest1() {
        //1. 고객 정보 확인
        // 1. 고객 정보 확인 (정상 로그인)
        LoginUserDTO customerLoginDto = new LoginUserDTO();
        customerLoginDto.setUserId("user102");
        customerLoginDto.setUserPwd("pwd102");

        try {
            Object loggedInCustomer = facadeService.loginUser(customerLoginDto);
            assertNotNull(loggedInCustomer);
            System.out.println(loggedInCustomer);
        } catch (FailedLoginException e) {
            fail("고객 로그인 실패: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("Read 로그인 테스트2: 샵 생성을 통해 사용자를 관리자로 변경하고 로그인으로 확인")
    void promoteUserToAdminViaShopCreationAndVerifyLogin() throws FailedLoginException {
        // When: 새로 회원가입한 isAdmin=0인 'user101'가 샵을 생성합니다.
        ShopCreationDTO shopCreationDTO = new ShopCreationDTO();
        shopCreationDTO.setShopName("신장개업");
        shopCreationDTO.setCategoryCode(6);
        shopCreationDTO.setAdminCode(101); //관리자가 되고자 하는 사용자code
        shopCreationDTO.setShopLocation("서울시 강남구");
        shopCreationDTO.setShopPhone("010-1234-5678");
        shopCreationDTO.setShopOpen("09:00");
        shopCreationDTO.setShopClose("18:00");

        // createShop 메서드를 호출하여 샵을 생성하고, 동시에 사용자의 권한을 변경합니다.
        ShopDTO createdShop = shopService.createShop(shopCreationDTO);

        // 샵 생성이 성공했는지 확인합니다.
        assertNotNull(createdShop);

        // Then: 'user101'가 이제 관리자인지 로그인으로 확인합니다.
        LoginUserDTO loginAttempt = new LoginUserDTO();
        loginAttempt.setUserId("user101");
        loginAttempt.setUserPwd("pwd101");

        // 로그인 작업을 수행합니다.
        LoginUserDTO loggedInUser = (LoginUserDTO) facadeService.loginUser(loginAttempt);

        // 로그인 결과가 null이 아닌지 확인합니다.
        assertNotNull(loggedInUser);
        assertNotNull(loggedInUser.getUserName());
        assertNotNull(loggedInUser.isAdmin());

        // 사용자 ID와 비밀번호가 기대하는 바와 같은 값인지 확인합니다.
        assertEquals("user101", loggedInUser.getUserId());
        assertEquals("TolkongLim", loggedInUser.getUserName());

        // **가장 중요**: 사용자가 관리자(isAdmin=1)로 변경되었는지 확인
        //assertTrue(loggedInUser.isAdmin());
        System.out.println("관리자 정보 확인 (샵 생성 후 로그인): " + loggedInUser);
    }

    @Test
    @DisplayName("Read 로그인 테스트3")
    void facadeLoginTest3 () {
        //3. 존재하지 않는 userId로 유저 정보 불러오기
        // when
        LoginUserDTO nonExistUserDto = new LoginUserDTO();
        nonExistUserDto.setUserId("nouser"); // 존재하지 않는 아이디
        nonExistUserDto.setUserPwd("some_password");

        // then: FailedLoginException 발생 확인
        FailedLoginException thrownLoginException = assertThrows(
                FailedLoginException.class,
                () -> facadeService.loginUser(nonExistUserDto)
        );
        assertTrue(thrownLoginException.getMessage().contains(" 유저를 찾을 수 없습니다."));
        System.out.println("존재하지 않는 유저 로그인 에러 메시지: " + thrownLoginException.getMessage());

        //3-1. 탈퇴한 userId(isLeave=1)로 유저 정보 불러오기
        // when
        LoginUserDTO leaveUser = new LoginUserDTO();
        leaveUser.setUserId("kimgurae_test"); // 탈퇴한 사용자
        leaveUser.setUserPwd("kimgurae_pwd");

        // then: FailedLoginException 발생 확인
        FailedLoginException thrownLoginException2 = assertThrows(
                FailedLoginException.class,
                () -> facadeService.loginUser(leaveUser)
        );
        assertTrue(thrownLoginException2.getMessage().contains(" 유저를 찾을 수 없습니다."));
        System.out.println("존재하지 않는 유저 로그인 에러 메시지: " + thrownLoginException2.getMessage());
    }

    @Test
    @DisplayName("Read 로그인 테스트4 : 잘못된 비밀번호로 로그인 시도")
    void facadeLoginTest4 () {
        LoginUserDTO wrongPwdUserDto = new LoginUserDTO();
        wrongPwdUserDto.setUserId("leeyounghee");
        wrongPwdUserDto.setUserPwd("wrong_password");

        FailedLoginException thrownWrongPwdException = assertThrows(
                FailedLoginException.class,
                () -> facadeService.loginUser(wrongPwdUserDto)
        );
        assertEquals("잘못된 비밀번호입니다.", thrownWrongPwdException.getMessage());
        System.out.println("잘못된 비밀번호 로그인 에러 메시지: " + thrownWrongPwdException.getMessage());
    }

    @Test
    @DisplayName("Update 유저 정보 수정-1 비밀번호 수정 시 메세지 반환 확인")
    void modifyUserTest() {
        // given: 기존 사용자 정보 조회
        LoginUserDTO dbuser = new LoginUserDTO();
        dbuser.setUserCode(6);
        dbuser.setUserId("choiminsu");
        dbuser.setUserPwd("choiminsu");

        // 등록된 유저의 userCode를 사용하여 업데이트 DTO 생성
        UserDTO updateDTO = new UserDTO();
        updateDTO.setUserCode(dbuser.getUserCode());
        updateDTO.setUserId(dbuser.getUserId()); // ID는 변경되지 않으므로 그대로 사용
        updateDTO.setUserPwd("choiminsu"); // 동일한 비밀번호로 수정 시도
        System.out.println("original updateDTO: " + updateDTO);

        // when1. 동일한 비밀번호로 수정 시도
        String result = facadeService.updateUser(updateDTO);

        // then(동일한 값에 대한 경고 메시지 확인)
        assertTrue(result.contains(ApiResponse.SAME_PASSWORD.getMessage()), "동일한 비밀번호 메시지가 포함되어야 합니다.");
        System.out.println("동일한 비밀번호 메시지: " + result);

        // when2. 새로운 비밀번호(다른 값)로 바꾸는 경우
        updateDTO.setUserPwd("newPwd");
        String successResult = facadeService.updateUser(updateDTO);

        // then: 비밀번호 수정 성공 메시지 확인
        assertEquals(ApiResponse.SUCCESS_MODIFY_USER.getMessage(), successResult, "비밀번호 수정 성공 메시지가 일치해야 합니다.");
        System.out.println("비밀번호 수정 성공 메시지: " + successResult);
        System.out.println("NewPwd 적용 후 updateDTO: " + updateDTO);

        // Test for "해당 유저가 존재하지 않습니다." (UserFacadeService.updateUser 내부에서 UserService.modifyUser가 던질 수 있음)
        UserDTO nonExistentUserDTO = new UserDTO();
        nonExistentUserDTO.setUserCode(9999); // 존재하지 않는 userCode
        nonExistentUserDTO.setUserId("nonexistent_update_pwd");
        nonExistentUserDTO.setUserPwd("some_pwd");

        IllegalArgumentException thrownException = assertThrows(
                IllegalArgumentException.class,
                () -> facadeService.updateUser(nonExistentUserDTO),
                "존재하지 않는 유저 정보 수정 시 IllegalArgumentException이 발생해야 합니다."
        );
        assertEquals("해당 유저가 존재하지 않습니다.", thrownException.getMessage());
    }

    @Test
    @DisplayName("Update 유저 정보 수정-2 전화번호 수정 시 메세지 반환 및 중복 전화번호 확인")
    void modifyUserTest2() {
        // given: 기존 사용자 정보 조회
        LoginUserDTO dbuser = new LoginUserDTO();
        dbuser.setUserCode(35);
        dbuser.setUserId("parkshinhye");
        dbuser.setUserPwd("parkshinhye");
        dbuser.setUserPhone("010-1035-1035");

        UserDTO updateDTO2 = new UserDTO();
        updateDTO2.setUserCode(dbuser.getUserCode());
        updateDTO2.setUserId(dbuser.getUserId());
        updateDTO2.setUserPhone(dbuser.getUserPhone());
        System.out.println("original: " + updateDTO2);
        //original: UserDTO(userCode=35, userId=parkshinhye, userPwd=null, isAdmin=0, userName=null, userPhone=010-1035-1035, birthday=null, isLeave=0)

        // when1. 동일한 정보로 수정 시도
        String result2 = facadeService.updateUser(updateDTO2);

        // then: 동일한 값에 대한 경고 메시지 확인
        System.out.println("Same modifying result: " + result2);
        //Same modifying result: 010-1035-1035(은)는 이전 전화번호와 동일합니다.

        assertTrue(result2.contains(ApiResponse.SAME_PHONE.getMessage()));
        //result2에 SAME_PHONE의 msg값이 들어가 있는지 확인

        // when2. 다른 값으로 바꾸는 경우
        updateDTO2.setUserPhone("010-1234-2222");
        String successResult = facadeService.updateUser(updateDTO2);

        // then 변경 값 학인
        assertEquals(ApiResponse.SUCCESS_MODIFY_USER.getMessage(), successResult);
        System.out.println("전화번호 수정 성공 메시지: " + successResult);
        //전화번호 수정 성공 메시지: 회원 정보가 성공적으로 수정되었습니다.
        System.out.println("After modify phone: " + updateDTO2);
        //After modify phone: UserDTO(userCode=35, userId=parkshinhye, userPwd=null, isAdmin=0, userName=null, userPhone=010-1234-2222, birthday=null, isLeave=0))
    }

    @Test
    @DisplayName("Update 유저 정보 수정-3 이름 수정 시 메세지 반환 확인")
    void modifyUserTest3() {
        // given: 테스트를 위해 사용자 등록
        LoginUserDTO registeredUser = new LoginUserDTO();
        registeredUser.setUserCode(29);
        registeredUser.setUserId("suzy29");
        registeredUser.setUserPwd("suzy29");

        // 등록된 유저의 userCode를 사용하여 업데이트 DTO 생성
        UserDTO updateDTO3 = new UserDTO();
        updateDTO3.setUserCode(registeredUser.getUserCode());
        updateDTO3.setUserId(registeredUser.getUserId());
        updateDTO3.setUserName("수지"); // 동일한 이름으로 수정 시도
        System.out.println("original updateDTO3: " + updateDTO3);

        // when1. 동일한 정보로 수정 시도
        String result = facadeService.updateUser(updateDTO3);

        // then: 동일한 값에 대한 경고 메시지 확인
        System.out.println("ModifyUser result: " + result);

        // when2. 다른 값으로 바꾸는 경우
        updateDTO3.setUserName("배수지");
        String successResult = facadeService.updateUser(updateDTO3);

        // then: 이름 수정 성공 메시지 확인
        assertEquals(ApiResponse.SUCCESS_MODIFY_USER.getMessage(), successResult, "이름 수정 성공 메시지가 일치해야 합니다.");
        System.out.println("이름 수정 성공 메시지: " + successResult);
        System.out.println("After modify name: " + updateDTO3);

        // Test for "해당 유저가 존재하지 않습니다."
        UserDTO nonExistentUserDTO = new UserDTO();
        nonExistentUserDTO.setUserCode(9999); // 존재하지 않는 userCode
        nonExistentUserDTO.setUserId("nonexistent_update_name");
        nonExistentUserDTO.setUserName("Some Name");

        IllegalArgumentException thrownException = assertThrows(
                IllegalArgumentException.class,
                () -> facadeService.updateUser(nonExistentUserDTO),
                "존재하지 않는 유저 정보 수정 시 IllegalArgumentException이 발생해야 합니다."
        );
        assertEquals("해당 유저가 존재하지 않습니다.", thrownException.getMessage(), "존재하지 않는 유저 에러 메시지가 일치해야 합니다.");
    }

    @Test
    @DisplayName("withdrawUser() - 회원탈퇴 : 논리적 삭제(isLeave=true) 확인 테스트")
    void withdrawUserTest() {
        // given: 테스트를 위해 사용자 등록
        UserDTO userToWithdraw = new UserDTO();
        userToWithdraw.setUserId("kimgurae_test");
        userToWithdraw.setUserPwd("kimgurae_pwd");
        userToWithdraw.setUserName("김구래");
        userToWithdraw.setUserPhone("010-9999-0000");
        userToWithdraw.setBirthday(LocalDate.parse("1980-01-01"));
        facadeService.registerUser(userToWithdraw);

        UserDTO dto = new UserDTO();
        dto.setUserId("kimgurae_test"); // 등록된 유저의 ID 사용
        System.out.println("original withdraw DTO:" + dto);

        // when: 회원 탈퇴 서비스 호출
        facadeService.withdrawUser(dto);

        // then: DB에서 다시 꺼내서 isLeave가 true로 바뀌었는지 확인
        User updatedUser = userRepository.findByUserId("kimgurae_test");
        assertNotNull(updatedUser, "탈퇴한 유저도 DB에 존재해야 하며 isLeave=true여야 합니다.");
        assertTrue(updatedUser.isLeave(), "회원 탈퇴 후 isLeave 값이 true여야 합니다.");
        System.out.println("isLeave 값: " + updatedUser.isLeave());
    }

    @Test
    @DisplayName("loadUserByUserId() - 인증을 위한 loadByUserID 사용 테스트")
    void loadUserByUserIdTest() {
        // Given: 테스트를 위해 사용자 등록
        UserDTO userForAuth = new UserDTO();
        userForAuth.setUserId("testuser_auth");
        userForAuth.setUserPwd("testpwd_auth");
        userForAuth.setUserName("태스토");
        userForAuth.setUserPhone("010-1111-0000");
        userForAuth.setBirthday(LocalDate.parse("2000-01-01"));
        facadeService.registerUser(userForAuth);

        // When: User exists
        AuthDetails authDetails = facadeService.loadUserByUserId("testuser_auth");
        assertNotNull(authDetails);
        assertEquals("testuser_auth", authDetails.getUsername(), "AuthDetails의 유저 ID가 일치해야 함.");
        System.out.println("Existing user 'testuser_auth' found by loadUserByUserId: " + authDetails.getUsername());

        // When: User does not exist
        UsernameNotFoundException thrownException = assertThrows(
                UsernameNotFoundException.class,
                () -> facadeService.loadUserByUserId("nonexistent_auth_user"),
                "존재하지 않는 유저 조회 시 UsernameNotFoundException이 발생해야 함."
        );
        assertEquals("해당하는 회원이 없습니다. 회원 가입을 먼저 진행해주십시오.", thrownException.getMessage(), "인가 시 존재하지 않는 유저 에러 메시지가 일치해야 합니다.");
        System.out.println("Non-existent user 'nonexistent_auth_user' test: " + thrownException.getMessage());
    }
}