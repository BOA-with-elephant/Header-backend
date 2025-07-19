package com.header.header.domain.user.service;

import com.header.header.auth.model.AuthDetails;
import com.header.header.auth.model.dto.LoginUserDTO;
import com.header.header.auth.model.dto.SignupDTO;
import com.header.header.domain.user.dto.UserDTO;
import com.header.header.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Objects;

import static com.header.header.auth.common.ApiResponse.SUCCESS_REGISTER_USER;

@Service
@RequiredArgsConstructor
public class UserFacadeService {

    private final UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /* 1. 더미 유저 등록 (전화번호/이름 기반)
    * 업장에서 전화번호 + 이름만으로 임시 가입한 dummy user 생성 */
    @Transactional
    public User registerDummyUser(String userName, String userPhone) {
        return userService.createUserByNameAndPhone(userName, userPhone);
    }

    /* 2. 이름/전화번호로 유저 코드 조회 */
    public Integer getUserCodeByNameAndPhone(String name, String phone) {
        return userService.findUserByNameAndPhone(name, phone);
    }

    /* 3. 회원가입 처리 (중복검사 포함) */
    @Transactional
    public int registerUser(SignupDTO signupDTO) {
        // 비밀번호 암호화
        signupDTO.setUserPwd(passwordEncoder.encode(signupDTO.getUserPwd()));

        String resultMessage = userService.registerNewUser(signupDTO);

        // Convert the String message to an int for the controller
        if (resultMessage.equals(SUCCESS_REGISTER_USER.getMessage())) {
            return 1; // Or any positive integer to indicate success
        } else {
            return 0; // Or any non-positive integer to indicate failure
        }
    }

    /* 4. 로그인 시 유저 정보 조회 */
    public LoginUserDTO login(String userId) {
        return userService.findByUserId(userId);
    }

    /* 5. 회원 정보 수정 */
    @Transactional
    public String updateUser(UserDTO dto) {
        return userService.modifyUser(dto);
    }

    /* 6. 회원 탈퇴 처리 (isLeave = true) */
    @Transactional
    public void withdrawUser(UserDTO dto) {
        userService.deleteUser(dto);
    }

    /* 7. 인가를 위한 loadByUserID 사용 */
    @Transactional
    public AuthDetails loadUserByUserId(String userId) {
        LoginUserDTO login = userService.findByUserId(userId);
        if (Objects.isNull(login)) {
            throw new UsernameNotFoundException("해당하는 회원이 없습니다.");
        }

        return new AuthDetails(login);
    }
}