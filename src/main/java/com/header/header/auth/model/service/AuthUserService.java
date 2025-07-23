package com.header.header.auth.model.service;

import com.header.header.auth.common.ApiResponse;
import com.header.header.auth.config.JwtTokenProvider;
import com.header.header.auth.exception.DuplicatedUserIdException;
import com.header.header.auth.exception.DuplicatedPhoneException;
import com.header.header.auth.exception.RegistrationUnknownException;
import com.header.header.auth.model.AuthDetails;
import com.header.header.auth.model.dto.LoginUserDTO;
import com.header.header.auth.model.dto.TokenDTO;
import com.header.header.domain.user.dto.UserDTO;
import com.header.header.domain.user.entity.User;
import com.header.header.domain.user.repository.MainUserRepository;
import com.header.header.domain.user.service.UserService;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.FailedLoginException;

@Service
public class AuthUserService implements UserDetailsService {
    @Autowired
    private UserService userService;

    private static final Logger log = LoggerFactory.getLogger(AuthUserService.class);
    private final MainUserRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MainUserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthUserService(MainUserRepository memberRepository, PasswordEncoder passwordEncoder, MainUserRepository userRepository, JwtTokenProvider jwtTokenProvider, UserService userService) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
    }

    public TokenDTO login(LoginUserDTO loginUserDTO) throws FailedLoginException {

        log.info("[AuthService] login() START");
        log.info("[AuthService] {}", loginUserDTO);

        /* 목차. 1. 아이디 조회 */
        User user = memberRepository.findByUserId(loginUserDTO.getUserId());

        if (user == null || user.isLeave()) {
            log.info("[AuthService] login() Required User Not Found!");
            throw new FailedLoginException(loginUserDTO.getUserId() + " 유저를 찾을 수 없습니다.");
        }

        /* 목차. 2. 비밀번호 매칭 */
        if (!passwordEncoder.matches(loginUserDTO.getUserPwd(), user.getUserPwd())) {
            log.info("[AuthService] Password Match Failed!");
            throw new FailedLoginException("잘못된 비밀번호입니다.");
        }

        TokenDTO tokenDto = jwtTokenProvider.generateTokenDTO(loginUserDTO);

        log.info("[AuthService] login() Token Generated: {}", tokenDto);
        log.info("[AuthService] login() END");

        return tokenDto; // TokenDTO 객체를 직접 반환
    }

    /** save : registerNewUser
     -> UserDTO 사용
     @param userDTO 생성할 user 정보가 담긴 DTO
     @return 생성된 signupDTO(user관련 DTO)
     이미 존재하는 아이디나 전화번호일 때 */
    public UserDTO signup(UserDTO userDTO) {
        log.info("[AuthService] Let's start signup().");
        log.info("[AuthService] userDTO {}", userDTO);

        /* 1. 중복 유효성 검사 */
        // 중복확인 1 : userId
        if (userRepository.existsByUserId(userDTO.getUserId())) {
            log.info("[AuthService] 아이디 중복");
            throw new DuplicatedUserIdException(ApiResponse.DUPLICATE_ID.getMessage());
        }
        // 중복확인 2 : userPhone
        if (userRepository.existsByUserPhone(userDTO.getUserPhone())) {
            log.info("[AuthService] 전화번호 중복");
            throw new DuplicatedPhoneException(ApiResponse.DUPLICATE_PHONE.getMessage());
        }

        try {
            // 2. 비밀번호 암호화
            String encodedPassword = passwordEncoder.encode(userDTO.getUserPwd());

            // 3. User 엔티티 생성 및 기본 권한(isAdmin=false) 설정
            User registUser = new User(
                    userDTO.getUserId(),
                    encodedPassword,
                    false, // isAdmin: 회원가입 시 기본값은 false (일반 사용자)
                    userDTO.getUserName(),
                    userDTO.getUserPhone(),
                    userDTO.getBirthday(),
                    false  // isLeave: 회원가입 시 기본값은 false (탈퇴하지 않음)
            );

            // 4. User 엔티티 저장
            User result = userRepository.save(registUser);

            /* 설명. 위의 save()가 성공해야 해당 트랜잭션이 성공했다고 판단. */
            log.info("[AuthService] 유저 생성 결과, {}",
                    (result != null) ? "회원 가입 성공" : "회원 가입 실패");

            return userDTO;

        } catch (Exception e) {
            log.error("회원가입 중 알 수 없는 오류 발생", e);
            throw new RegistrationUnknownException(e);
        } finally {
            log.info("[AuthService] signup() End.");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {

        if(userId == null || userId.equals("")) {
            throw new AuthenticationServiceException(userId + "아이디를 입력하지 않았습니다.");
        } else {
            LoginUserDTO loginUserDTO = userService.findByUserId(userId);
            if (loginUserDTO == null) {
                throw new UsernameNotFoundException("해당하는 회원이 없습니다. 회원 가입 후 로그인 해주십시오.");
            }
            return new AuthDetails(loginUserDTO);
        }
    }
}