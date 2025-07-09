package com.header.header.domain.auth;

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

import java.time.LocalDate;
import java.util.Optional;

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
        assertEquals(41, checkUserIn.getUserCode());
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
    @DisplayName("modifyUser - 유저 정보 수정 성공 테스트")
    void modifyUser_success_test() {
        //given
        AuthUserDTO updateDTO = new AuthUserDTO();
        updateDTO.setUserCode(29);
        updateDTO.setUserPwd("newPwd29");
        updateDTO.setUserPhone("010-1111-2222");
        updateDTO.setUserName("배수지");

        // when
        authUserService.modifyUser(updateDTO);

        // then
        User updatedUser = authUserRepository.findById(29).orElseThrow();
        assertEquals("newPwd29", updatedUser.getUserPwd());
        assertEquals("010-1111-2222", updatedUser.getUserPhone());
        assertEquals("배수지", updatedUser.getUserName());

        System.out.println(updateDTO);
        //AuthUserDTO(userCode=29, userId=null, userPwd=newPwd29, isAdmin=false, userName=배수지, userPhone=010-1111-2222, birthday=null, isLeave=false)
    }

    @Test
    @DisplayName("Delete(논리적 삭제, isLeave=true) 테스트")
    void deleteUserTest() {
        //given
        AuthUserDTO checkLeaved = new AuthUserDTO();
        checkLeaved.setUserCode(30);
        checkLeaved.setUserId("user30");
        checkLeaved.setUserPwd("pwd30");
        checkLeaved.setUserName("정해인");
        checkLeaved.setUserPhone("010-1233-2567");
        checkLeaved.setLeave(false);
        System.out.println(checkLeaved);
        //AuthUserDTO(userCode=30, userId=user30, userPwd=pwd30, isAdmin=false, userName=정해인, userPhone=010-1233-2567, birthday=null, isLeave=false)

        //when
        //isLeave가 false(기본값)이라면
        assertFalse(checkLeaved.isLeave());

        //then
        checkLeaved.setLeave(true);

        assertTrue(checkLeaved.isLeave());
        System.out.println(checkLeaved);
        //AuthUserDTO(userCode=30, userId=user30, userPwd=pwd30, isAdmin=false, userName=정해인, userPhone=010-1233-2567, birthday=null, isLeave=true)
    }
}
