package com.header.header.domain.user.service;

import com.header.header.auth.common.ApiResponse;
import com.header.header.auth.exception.DuplicatedPhoneException;
import com.header.header.auth.exception.DuplicatedUserIdException;
import com.header.header.auth.exception.RegistrationUnknownException;
import com.header.header.auth.model.AuthDetails;
import com.header.header.auth.model.dto.LoginUserDTO;
import com.header.header.auth.model.dto.SignupDTO;
import com.header.header.auth.model.service.AuthUserService;
import com.header.header.domain.user.dto.UserDTO;
import com.header.header.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.security.auth.login.FailedLoginException;
import java.util.Objects;

import static com.header.header.auth.common.ApiResponse.SUCCESS_REGISTER_USER;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserFacadeService {

    private final UserService userService;

    private final AuthUserService authService;

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
    public UserDTO registerUser(UserDTO userDTO) { // SignupDTO 대신 UserDTO를 파라미터로 받음
        log.info("[UserFacadeService] registerUser() started with UserDTO: {}", userDTO);

        try {
            // AuthService의 signup 메소드를 호출하여 회원가입 처리
            // 비밀번호 암호화 및 중복 검사는 AuthService 내부에서 모두 처리됩니다.
            UserDTO registeredUser = authService.signup(userDTO);
            log.info("[UserFacadeService] User registration successful for user: {}", registeredUser.getUserId());
            return registeredUser;
        } catch (DuplicatedUserIdException e) {
            log.warn("[UserFacadeService] Duplicated User ID: {}", e.getMessage());
            throw e; // Facade에서 예외를 다시 던져 컨트롤러나 @ControllerAdvice에서 처리하도록 함
        } catch (DuplicatedPhoneException e) {
            log.warn("[UserFacadeService] Duplicated Phone: {}", e.getMessage());
            throw e; // Facade에서 예외를 다시 던져 컨트롤러나 @ControllerAdvice에서 처리하도록 함
        } catch (RegistrationUnknownException e) {
            log.error("[UserFacadeService] Unknown error during registration: {}", e.getMessage(), e);
            throw e; // Facade에서 예외를 다시 던져 컨트롤러나 @ControllerAdvice에서 처리하도록 함
        } catch (Exception e) { // 예상치 못한 다른 예외 처리
            log.error("[UserFacadeService] An unexpected error occurred: {}", e.getMessage(), e);
            throw new RegistrationUnknownException(e);
        }
    }

    /* 4. 로그인 시 유저 정보 조회 */
    public Object login(LoginUserDTO loginUserDTO) throws FailedLoginException {
        return authService.loginUser(loginUserDTO);
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