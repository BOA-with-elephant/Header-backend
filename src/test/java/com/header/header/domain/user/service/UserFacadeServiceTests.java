package com.header.header.domain.user.service;

import com.header.header.auth.common.ApiResponse;
import com.header.header.domain.user.controller.UserController;
import com.header.header.domain.user.dto.UserDTO;
import com.header.header.auth.model.dto.LoginUserDTO;
import com.header.header.auth.model.dto.SignupDTO;
import com.header.header.domain.user.entity.User;
import com.header.header.domain.user.repository.MainUserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ComponentScan(basePackages = "com.header.header")
@Transactional // 테스트 후 데이터 롤백
public class UserFacadeServiceTests {

    @Autowired
    private UserFacadeService facadeService;

    @Autowired
    private MainUserRepository userRepository;

    @Test
    @DisplayName("회원가입(user 생성) + 중복 체크 테스트")
    void registerUserTest() {
        //given1. 기존 정보와 중복 없이 가입할 때
        UserDTO newSignup = new UserDTO();
        newSignup.setUserId("user41");
        newSignup.setUserPwd("user41pwd");
        newSignup.setUserName("Kim tolkong");
        newSignup.setUserPhone("010-1233-2567");
        newSignup.setBirthday(LocalDate.parse("2000-12-31"));

        //when
        Object result1 = facadeService.registerUser(newSignup);
        //then
        System.out.println("가입 성공 메세지: " + result1);
        System.out.println("가입 정보 확인: "+ newSignup);
        //가입 성공 메세지: 회원가입이 완료되었습니다.
        //가입 정보 확인: SignupDTO(userCode=101, userName=Kim tolkong, userPhone=010-1233-2567, userId=user41, userPwd=$2a$10$L3Md/JUWLKOGKBEwOSunD.VaonwaE.SAxZW8KZX5KcICbPBb7LlDG, birthday=2000-12-31)

        //given2. 중복된 userId 사용
        UserDTO duplicateIdDTO = new UserDTO();
        duplicateIdDTO.setUserId("kwoneunji"); // DB에 존재
        duplicateIdDTO.setUserPwd("test1234");
        duplicateIdDTO.setUserName("Test User");
        duplicateIdDTO.setUserPhone("010-9999-9999");
        duplicateIdDTO.setBirthday(LocalDate.parse("2025-07-21"));

        //when(중복 아이디 가입 시도)
        Object result2 = facadeService.registerUser(duplicateIdDTO);

        //then: 중복 아이디 메시지 확인
        assertEquals(ApiResponse.DUPLICATE_ID.getMessage(), result2);
        System.out.println("Check Id msg: "+result2);
        //Check Id msg: 이미 존재하는 아이디입니다.

        //given3. 중복된 전화번호로 가입 시도
        UserDTO duplicatePhoneDTO = new UserDTO();
        duplicatePhoneDTO.setUserId("newuser01");
        duplicatePhoneDTO.setUserPwd("pass001");
        duplicatePhoneDTO.setUserName("Jane Doe");
        duplicatePhoneDTO.setUserPhone("010-1004-1004"); // DB에 존재
        duplicatePhoneDTO.setBirthday(LocalDate.parse("2025-07-10"));

        //when
        Object result3 = facadeService.registerUser(duplicatePhoneDTO);

        //then: 중복 전화번호 메시지 확인
        assertEquals(ApiResponse.DUPLICATE_PHONE.getMessage(), result3);
        System.out.println("Check phone msg: "+ result3);
        //Check phone msg: 이미 존재하는 전화번호입니다.
    }

    @Test
    @DisplayName("Read 로그인 테스트")
    void facadeLoginTest() {
        //given
        //1. 고객 정보 확인
        //when
        LoginUserDTO checkLoggedIn = facadeService.login("leegahyeon");

        //then
        assertNotNull(checkLoggedIn);
        assertNotNull(checkLoggedIn.getUserCode());
        assertNotNull(checkLoggedIn.getUserName());
        assertNotNull(checkLoggedIn.isAdmin());

        assertEquals("leegahyeon", checkLoggedIn.getUserId());
        assertEquals("leegahyeon", checkLoggedIn.getUserPwd());
        assertEquals("이가현", checkLoggedIn.getUserName());
        assertFalse(checkLoggedIn.isAdmin());
        System.out.println("고객 정보 확인: " + checkLoggedIn);
        //고객 정보 확인: LoginUserDTO(userCode=98, userId=leegahyeon, userPwd=leegahyeon, userName=이가현, isAdmin=false)

        //2. 관리자 정보 확인
        //when
        LoginUserDTO checkAdmin = facadeService.login("kwoneunji");

        //then
        assertNotNull(checkAdmin);
        assertNotNull(checkAdmin.getUserCode());
        assertNotNull(checkAdmin.getUserName());
        assertNotNull(checkLoggedIn.isAdmin());

        assertEquals("kwoneunji", checkAdmin.getUserId());
        assertEquals("kwoneunji", checkAdmin.getUserPwd());
        assertEquals("권은지", checkAdmin.getUserName());
        assertTrue(checkAdmin.isAdmin());
        System.out.println("관리자 정보 확인: " +checkAdmin);
        //관리자 정보 확인: LoginUserDTO(userCode=2, userId=kwoneunji, userPwd=kwoneunji, userName=권은지, isAdmin=true)

        //3. 존재하지 않는 userId로 유저 정보 불러오기
        // given
        // when
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> facadeService.login("user01")  // 존재하지 않는 아이디
        );

        //then
        assertEquals("해당하는 회원이 없습니다. 회원 가입 후 로그인 해주십시오.", exception.getMessage());
    }

    @Test
    @DisplayName("Update 유저 정보 수정-1 비밀번호 수정 시 메세지 반환 확인")
    void modifyUserTest() {
        // given
        LoginUserDTO dbUser = facadeService.login("choiminsu");

        UserDTO updateDTO = new UserDTO();
        updateDTO.setUserCode(dbUser.getUserCode());
        updateDTO.setUserPwd(dbUser.getUserPwd()); // 동일한 비밀번호 설정
        System.out.println("original: " + updateDTO);

        // when1. 동일한 정보로 수정 시도
        String result = facadeService.updateUser(updateDTO);

        // then(동일한 값에 대한 경고 메시지 확인)
        assertTrue(result.contains(ApiResponse.SAME_PASSWORD.getMessage()));
        System.out.println("동일한 비밀번호 메시지: " + result);

        // when2. 새로운 비밀번호(다른 값)로 바꾸는 경우
        updateDTO.setUserPwd("newPwd");

        //then
        String successResult = facadeService.updateUser(updateDTO);
        System.out.println("NewPwd: " + updateDTO);

        assertEquals(ApiResponse.SUCCESS_MODIFY_USER.getMessage(), successResult);
        System.out.println("비밀번호 수정 성공 메시지: " + successResult);
        //비밀번호 수정 성공 메시지: 회원 정보가 성공적으로 수정되었습니다.
    }

    @Test
    @DisplayName("Update 유저 정보 수정-2 전화번호 수정 시 메세지 반환 및 중복 전화번호 확인")
    void modifyUserTest2() {
        // given: 기존 사용자 정보 조회
        LoginUserDTO dbuser = facadeService.login("parkshinhye");

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
        // given: 기존 사용자 정보 조회
        LoginUserDTO dbuser = facadeService.login("suzy29");

        UserDTO updateDTO3 = new UserDTO();
        updateDTO3.setUserCode(dbuser.getUserCode());
        updateDTO3.setUserName(dbuser.getUserName());
        System.out.println("original: " + updateDTO3);
        //original: UserDTO(userCode=29, userId=null, userPwd=null, isAdmin=0, userName=수지, userPhone=null, birthday=null, isLeave=0)

        // when1. 동일한 정보로 수정 시도
        String result = facadeService.updateUser(updateDTO3);

        // then: 동일한 값에 대한 경고 메시지 확인
        assertTrue(result.contains(ApiResponse.SAME_NAME.getMessage()));

        System.out.println("ModifyUser result: " + result);
        //ModifyUser result: 수지(은)는 이전 이름과 동일합니다.

        // when2. 다른 값으로 바꾸는 경우
        updateDTO3.setUserName("배수지");

        // then 변경 값 학인
        String successResult = facadeService.updateUser(updateDTO3);
        assertEquals(ApiResponse.SUCCESS_MODIFY_USER.getMessage(), successResult);
        System.out.println("이름 수정 성공 메시지: " + successResult);
        System.out.println("After modify name: " + updateDTO3);
        //이름 수정 성공 메시지: 회원 정보가 성공적으로 수정되었습니다.
        //After modify name: UserDTO(userCode=29, userId=null, userPwd=null, isAdmin=0, userName=배수지, userPhone=null, birthday=null, isLeave=0)
    }

    @Test
    @DisplayName("withdrawUser() - 회원탈퇴 : 논리적 삭제(isLeave=true) 확인 테스트")
    void withdrawUserTest() {
        // given
        String userId = "kimgurae";
        UserDTO dto = new UserDTO();
        dto.setUserId(userId);
        System.out.println("original:" + dto);
        //original:UserDTO(userCode=0, userId=kimgurae, userPwd=null, isAdmin=0, userName=null, userPhone=null, birthday=null, isLeave=0)

        // when
        facadeService.withdrawUser(dto);

        // then: DB에서 다시 꺼내서 isLeave가 true로 바뀌었는지 확인
        User updatedUser = userRepository.findByUserId(userId);
        assertTrue(updatedUser.isLeave(), "이미 탈퇴한 회원입니다");
        System.out.println("isLeave 값: " + updatedUser.isLeave());
        //isLeave 값: true
    }
}