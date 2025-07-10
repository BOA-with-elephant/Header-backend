package com.header.header.domain.auth;

import com.header.header.domain.auth.common.ApiResponse;
import com.header.header.domain.auth.model.dto.AuthUserDTO;
import com.header.header.domain.auth.model.dto.LoginUserDTO;
import com.header.header.domain.auth.model.dto.SignupDTO;
import com.header.header.domain.auth.model.repository.AuthUserRepository;
import com.header.header.domain.auth.model.service.AuthUserService;
import com.header.header.domain.user.entity.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // 테스트 후 데이터 롤백
public class AuthUserServiceTests {
    @Autowired
    private AuthUserService authUserService;

    @Autowired
    private AuthUserRepository authUserRepository;

    @Test
    @DisplayName("회원가입(user 생성) 테스트")
    void registerNewUserTest() {
        //given
        SignupDTO newSignup = new SignupDTO();
        newSignup.setUserId("user41");
        newSignup.setUserPwd("user41pwd");
        newSignup.setUserName("Kim tolkong");
        newSignup.setUserPhone("010-1233-2567");
        newSignup.setBirthday("2000-12-31");

        //when
        SignupDTO checkUserIn = authUserService.registerNewUser(newSignup);

        //then
        assertNotNull(checkUserIn);
        assertNotNull(checkUserIn.getUserCode());
        System.out.println(checkUserIn.getUserCode());
        assertEquals("user41", checkUserIn.getUserId());
        assertEquals("user41pwd", checkUserIn.getUserPwd());
        assertEquals("Kim tolkong", checkUserIn.getUserName());
        assertEquals("010-1233-2567", checkUserIn.getUserPhone());
        assertEquals("2000-12-31", checkUserIn.getBirthday());
    }

    @Test
    @DisplayName("Read 로그인 테스트")
    void loginUserTest() {
        //given
        //1. 고객 정보 확인
        //when
        LoginUserDTO checkLoggedIn = authUserService.findUserByUserId(3);

        //then
        assertNotNull(checkLoggedIn);
        assertNotNull(checkLoggedIn.getUserCode());
        assertNotNull(checkLoggedIn.getUserName());

        assertEquals("user03", checkLoggedIn.getUserId());
        assertEquals("pwd03", checkLoggedIn.getUserPwd());
        assertEquals("홍길동", checkLoggedIn.getUserName());
        assertFalse(checkLoggedIn.isAdmin());

        //2. 관리자 정보 확인
        //when
        LoginUserDTO checkAdmin = authUserService.findUserByUserId(2);

        //then
        assertNotNull(checkAdmin);
        assertNotNull(checkAdmin.getUserCode());
        assertNotNull(checkAdmin.getUserName());

        assertEquals("admin02", checkAdmin.getUserId());
        assertEquals("pwd02", checkAdmin.getUserPwd());
        assertEquals("권은지", checkAdmin.getUserName());
        assertTrue(checkAdmin.isAdmin());

        //3. 존재하지 않는 userCode로 유저 정보 불러오기
        //when
        
        //3. 존재하지 않는 userCode로 유저 정보 불러오기
        //when
        //LoginUserDTO checkNoUser = authUserService.findUserByUserId(31);
        //exception : 해당하는 회원이 없습니다. 회원가입 후 로그인 해주십시오.
    }

    @Test
    @DisplayName("Update 유저 정보 수정-1 비밀번호 수정 시 메세지 반환 확인")
    void modifyUserTest() {
        // given: 기존 사용자 정보 DTO에 받아 조회
        User dbuser = authUserRepository.findById(29).orElseThrow();

        AuthUserDTO updateDTO = new AuthUserDTO();
        updateDTO.setUserCode(dbuser.getUserCode());
        updateDTO.setUserPwd(dbuser.getUserPwd());
        updateDTO.setUserPhone(dbuser.getUserPhone());
        updateDTO.setUserName(dbuser.getUserName());
        System.out.println("original: " + updateDTO);
        //original: AuthUserDTO(userCode=29, userId=null, userPwd=pwd29, isAdmin=false, userName=수지, userPhone=010-1029-1029, birthday=null, isLeave=false)

        // when1. 동일한 정보로 수정 시도
        String result = authUserService.modifyUser(updateDTO);

        // then: 동일한 값에 대한 경고 메시지 확인
        assertEquals(ApiResponse.SAME_PASSWORD.getMessage(), result);
        System.out.println("Modifying same result: " + result);
        //Modifying same result: 이전 비밀번호와 동일합니다.

        // when2. 다른 값으로 바꾸는 경우
        updateDTO.setUserPwd("newPwd29");

        //then
        assertEquals("newPwd29", updateDTO.getUserPwd());

        System.out.println("After modify pwd: " + updateDTO);
        //After modify pwd: AuthUserDTO(userCode=29, userId=null, userPwd=newPwd29, isAdmin=false, userName=수지, userPhone=010-1029-1029, birthday=null, isLeave=false)
    }

    @Test
    @DisplayName("Update 유저 정보 수정-2 전화번호 수정 시 메세지 반환 및 중복 전화번호 확인")
    void modifyUserTest2() {
        // given: 기존 사용자 정보 조회
        User dbuser = authUserRepository.findById(29).orElseThrow();

        AuthUserDTO updateDTO2 = new AuthUserDTO();
        updateDTO2.setUserCode(dbuser.getUserCode());
        updateDTO2.setUserPhone(dbuser.getUserPhone());
        System.out.println("original: " + updateDTO2);
        //original: AuthUserDTO(userCode=29, userId=null, userPwd=null, isAdmin=false, userName=null, userPhone=010-1029-1029, birthday=null, isLeave=false)

        // when1. 동일한 정보로 수정 시도
        String result2 = authUserService.modifyUser(updateDTO2);

        // then: 동일한 값에 대한 경고 메시지 확인
        System.out.println("Same modifying result: " + result2);
        //Same modifying result: 이전 전화번호와 동일합니다.

        assertEquals(ApiResponse.SAME_PHONE.getMessage(), result2);

        // when2. 다른 값으로 바꾸는 경우
        updateDTO2.setUserPhone("010-1234-2222");

        // then 변경 값 학인
        System.out.println("After modify phone: " + updateDTO2);
        //After modify phone: AuthUserDTO(userCode=29, userId=null, userPwd=null, isAdmin=false, userName=null, userPhone=010-1234-2222, birthday=null, isLeave=false)
    }

    @Test
    @DisplayName("Update 유저 정보 수정-3 이름 수정 시 메세지 반환 확인")
    void modifyUserTest3() {
        // given: 기존 사용자 정보 조회
        User dbuser = authUserRepository.findById(29).orElseThrow();

        AuthUserDTO updateDTO3 = new AuthUserDTO();
        updateDTO3.setUserCode(dbuser.getUserCode());
        updateDTO3.setUserName(dbuser.getUserName());
        System.out.println("original: " + updateDTO3);
        //original: AuthUserDTO(userCode=29, userId=null, userPwd=null, isAdmin=false, userName=수지, userPhone=null, birthday=null, isLeave=false)

        // when1. 동일한 정보로 수정 시도
        String result = authUserService.modifyUser(updateDTO3);

        // then: 동일한 값에 대한 경고 메시지 확인
        assertEquals(ApiResponse.SAME_NAME.getMessage(), result);

        System.out.println("ModifyUser result: " + result);
        //ModifyUser result: 이전 이름과 동일합니다.

        // when2. 다른 값으로 바꾸는 경우
        updateDTO3.setUserName("배수지");

        // then 변경 값 학인
        System.out.println("After modify name: " + updateDTO3);
        //After modify name: AuthUserDTO(userCode=29, userId=null, userPwd=null, isAdmin=false, userName=배수지, userPhone=null, birthday=null, isLeave=false)
    }

    @Test
    @DisplayName("deleteUser() - 논리적 삭제(isLeave=true) 확인 테스트")
    void deleteUserTest() {
        // given
        int userCode = 30;
        AuthUserDTO dto = new AuthUserDTO();
        dto.setUserCode(userCode);
        System.out.println("original:" + dto);
        //original:AuthUserDTO(userCode=30, userId=null, userPwd=null, isAdmin=false, userName=null, userPhone=null, birthday=null, isLeave=false)

        // when
        authUserService.deleteUser(dto);

        // then: DB에서 다시 꺼내서 isLeave가 true로 바뀌었는지 확인
        User updatedUser = authUserRepository.findById(userCode)
                .orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다"));

        assertTrue(updatedUser.isLeave());  // 논리 삭제되었는지 확인
        System.out.println("isLeave 값: " + updatedUser.isLeave());
        //isLeave 값: true
    }

}