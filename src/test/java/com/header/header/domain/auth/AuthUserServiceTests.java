package com.header.header.domain.auth;

import com.header.header.auth.common.ApiResponse;
import com.header.header.auth.model.dto.AuthUserDTO;
import com.header.header.auth.model.dto.LoginUserDTO;
import com.header.header.auth.model.dto.SignupDTO;
import com.header.header.auth.model.repository.AuthUserRepository;
import com.header.header.auth.model.service.AuthUserService;
import com.header.header.domain.user.entity.User;
import com.header.header.domain.user.repository.MainUserRepository;
import com.header.header.domain.user.service.UserService;
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
    private UserService userService;

    @Autowired
    private MainUserRepository userRepository;

    @Test
    @DisplayName("회원가입(user 생성) + 중복 체크 테스트")
    void registerNewUserTest() {
        //given1. 기존 정보와 중복 없이 가입할 때
        SignupDTO newSignup = new SignupDTO();
        newSignup.setUserId("user41");
        newSignup.setUserPwd("user41pwd");
        newSignup.setUserName("Kim tolkong");
        newSignup.setUserPhone("010-1233-2567");
        newSignup.setBirthday("2000-12-31");

        //when
        Object result1 = userService.registerNewUser(newSignup);

        //then
        System.out.print("가입 성공 메세지: " + result1);
        System.out.println("가입 정보 확인: "+ newSignup);

        //given2. 중복된 userId 사용
        SignupDTO duplicateIdDTO = new SignupDTO();
        duplicateIdDTO.setUserId("kwoneunji"); // DB에 존재
        duplicateIdDTO.setUserPwd("test1234");
        duplicateIdDTO.setUserName("Test User");
        duplicateIdDTO.setUserPhone("010-9999-9999");
        duplicateIdDTO.setBirthday("2025-07-14");

        //when(중복 아이디 가입 시도)
        Object result2 = userService.registerNewUser(duplicateIdDTO);

        //then: 중복 아이디 메시지 확인
        assertEquals(ApiResponse.DUPLICATE_ID.getMessage(), result2);
        System.out.println("Check Id msg: "+result2);

        //given3. 중복된 전화번호로 가입 시도
        SignupDTO duplicatePhoneDTO = new SignupDTO();
        duplicatePhoneDTO.setUserId("unique_user_001");
        duplicatePhoneDTO.setUserPwd("pass001");
        duplicatePhoneDTO.setUserName("Unique User");
        duplicatePhoneDTO.setUserPhone("010-1004-1004"); // DB에 존재
        duplicatePhoneDTO.setBirthday("2025-07-10");

        //when
        Object result3 = userService.registerNewUser(duplicatePhoneDTO);

        //then: 중복 전화번호 메시지 확인
        assertEquals(ApiResponse.DUPLICATE_PHONE.getMessage(), result3);
        System.out.println("Check phone msg: "+ result3);
    }

    @Test
    @DisplayName("Read 로그인 테스트")
    void loginUserTest() {
        //given
        //1. 고객 정보 확인
        //when
        LoginUserDTO checkLoggedIn = userService.findByUserId("leegahyeon");

        //then
        assertNotNull(checkLoggedIn);
        assertNotNull(checkLoggedIn.getUserCode());
        assertNotNull(checkLoggedIn.getUserName());
        assertNotNull(checkLoggedIn.getUserRole().getRole());

        assertEquals("leegahyeon", checkLoggedIn.getUserId());
        assertEquals("leegahyeon", checkLoggedIn.getUserPwd());
        assertEquals("이가현", checkLoggedIn.getUserName());
        assertEquals("USER", checkLoggedIn.getUserRole().getRole());

        //2. 관리자 정보 확인
        //when
        LoginUserDTO checkAdmin = userService.findByUserId("kwoneunji");

        //then
        assertNotNull(checkAdmin);
        assertNotNull(checkAdmin.getUserCode());
        assertNotNull(checkAdmin.getUserName());
        assertNotNull(checkLoggedIn.getUserRole());

        assertEquals("kwoneunji", checkAdmin.getUserId());
        assertEquals("kwoneunji", checkAdmin.getUserPwd());
        assertEquals("권은지", checkAdmin.getUserName());
        assertEquals("ADMIN", checkLoggedIn.getUserRole().getRole());

        //3. 존재하지 않는 userCode로 유저 정보 불러오기
        //when
        //LoginUserDTO checkNoUser = authUserService.findUserByUserId(31);
        //exception : 해당하는 회원이 없습니다. 회원가입 후 로그인 해주십시오.
    }

    @Test
    @DisplayName("Update 유저 정보 수정-1 비밀번호 수정 시 메세지 반환 확인")
    void modifyUserTest() {
        User dbuser = userRepository.findById(29).orElseThrow();

        AuthUserDTO updateDTO = new AuthUserDTO();
        updateDTO.setUserCode(dbuser.getUserCode());
        updateDTO.setUserPwd(dbuser.getUserPwd());
        System.out.println("original: " + updateDTO);
        //original: AuthUserDTO(userCode=29, userId=null, userPwd=pwd29, isAdmin=false, userName=null, userPhone=null, birthday=null, isLeave=false)

        // when1. 동일한 정보로 수정 시도
        String result = userService.modifyUser(updateDTO);

        // then(동일한 값에 대한 경고 메시지 확인)
        assertTrue(result.contains(ApiResponse.SAME_PASSWORD.getMessage()));
        System.out.println("동일한 비밀번호 메시지: " + result);
        //동일한 비밀번호 메시지: pwd29(은)는 이전 비밀번호와 동일합니다.

        // when2. 새로운 비밀번호(다른 값으)로 바꾸는 경우
        updateDTO.setUserPwd("newPwd29");

        //then
        String successResult = userService.modifyUser(updateDTO);

        assertEquals(ApiResponse.SUCCESS_MODIFY_USER.getMessage(), successResult);
        System.out.println("비밀번호 수정 성공 메시지: " + successResult);
        //비밀번호 수정 성공 메시지: 회원 정보가 성공적으로 수정되었습니다.
    }

    @Test
    @DisplayName("Update 유저 정보 수정-2 전화번호 수정 시 메세지 반환 및 중복 전화번호 확인")
    void modifyUserTest2() {
        // given: 기존 사용자 정보 조회
        User dbuser = userRepository.findById(29).orElseThrow();

        AuthUserDTO updateDTO2 = new AuthUserDTO();
        updateDTO2.setUserCode(dbuser.getUserCode());
        updateDTO2.setUserPhone(dbuser.getUserPhone());
        System.out.println("original: " + updateDTO2);
        //original: AuthUserDTO(userCode=29, userId=null, userPwd=null, isAdmin=false, userName=null, userPhone=010-1029-1029, birthday=null, isLeave=false)

        // when1. 동일한 정보로 수정 시도
        String result2 = userService.modifyUser(updateDTO2);

        // then: 동일한 값에 대한 경고 메시지 확인
        System.out.println("Same modifying result: " + result2);
        //Same modifying result: 010-1029-1029(은)는 이전 전화번호와 동일합니다.

        assertTrue(result2.contains(ApiResponse.SAME_PHONE.getMessage()));
        //result2에 SAME_PHONE의 msg값이 들어가 있는지 확인

        // when2. 다른 값으로 바꾸는 경우
        updateDTO2.setUserPhone("010-1234-2222");
        String successResult = userService.modifyUser(updateDTO2);

        // then 변경 값 학인
        assertEquals(ApiResponse.SUCCESS_MODIFY_USER.getMessage(), successResult);
        System.out.println("전화번호 수정 성공 메시지: " + successResult);
        //전화번호 수정 성공 메시지: 회원 정보가 성공적으로 수정되었습니다.
        System.out.println("After modify phone: " + updateDTO2);
        //After modify phone: AuthUserDTO(userCode=29, userId=null, userPwd=null, isAdmin=false, userName=null, userPhone=010-1234-2222, birthday=null, isLeave=false)
    }

    @Test
    @DisplayName("Update 유저 정보 수정-3 이름 수정 시 메세지 반환 확인")
    void modifyUserTest3() {
        // given: 기존 사용자 정보 조회
        User dbuser = userRepository.findById(29).orElseThrow();

        AuthUserDTO updateDTO3 = new AuthUserDTO();
        updateDTO3.setUserCode(dbuser.getUserCode());
        updateDTO3.setUserName(dbuser.getUserName());
        System.out.println("original: " + updateDTO3);
        //original: AuthUserDTO(userCode=29, userId=null, userPwd=null, isAdmin=false, userName=수지, userPhone=null, birthday=null, isLeave=false)

        // when1. 동일한 정보로 수정 시도
        String result = userService.modifyUser(updateDTO3);

        // then: 동일한 값에 대한 경고 메시지 확인
        assertTrue(result.contains(ApiResponse.SAME_NAME.getMessage()));

        System.out.println("ModifyUser result: " + result);
        //ModifyUser result: 이전 이름과 동일합니다.

        // when2. 다른 값으로 바꾸는 경우
        updateDTO3.setUserName("배수지");

        // then 변경 값 학인
        String successResult = userService.modifyUser(updateDTO3);
        assertEquals(ApiResponse.SUCCESS_MODIFY_USER.getMessage(), successResult);
        System.out.println("이름 수정 성공 메시지: " + successResult);
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
        userService.deleteUser(dto);

        // then: DB에서 다시 꺼내서 isLeave가 true로 바뀌었는지 확인
        User updatedUser = userRepository.findById(userCode)
                .orElseThrow(() -> new NoSuchElementException("회원이 존재하지 않습니다"));

        assertTrue(updatedUser.isLeave());  // 논리 삭제되었는지 확인
        System.out.println("isLeave 값: " + updatedUser.isLeave());
        //isLeave 값: true
    }

}