package com.header.header.domain.user.controller;

import com.header.header.auth.model.AuthDetails;
import com.header.header.auth.model.dto.LoginUserDTO;
import com.header.header.auth.model.dto.TokenDTO;
import com.header.header.auth.model.service.AuthUserService;
import com.header.header.common.dto.ResponseDTO;
import com.header.header.domain.user.dto.UserDTO;
import com.header.header.domain.user.service.UserFacadeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.FailedLoginException;

@RestController
@Slf4j
@RequestMapping("/auth")
public class UserController {

    @Autowired
    private final UserFacadeService userFacadeService;
    @Autowired
    private AuthUserService authUserService;

    public UserController(UserFacadeService userFacadeService) {
        this.userFacadeService = userFacadeService;
    }

    /* 설명.
     *  @RequestBody를 통해 RequestBody로 넘어온 JSON 문자열을 파싱해 MemberDTO 속성으로 매핑해 객체로 받아낸다.
     *  (회원 아이디, 비밀번호)
     *  + 요청의 body에서 데이터를 뽑아내겠다는 것은 요청이 POST 요청이었다는 것을 알 수 있다.
     *  왜냐하면 GET 요청은 body가 아니라 header에 데이터가 담겨있기 때문이다.
     * */
    @PostMapping("/session")
    public ResponseEntity<ResponseDTO> login(@RequestBody LoginUserDTO loginUserDTO) throws FailedLoginException {
        try {
            // userFacadeService.loginUser 호출하여 TokenDTO 객체 직접 받기
            TokenDTO tokenDTO = userFacadeService.loginUser(loginUserDTO);

            return ResponseEntity
                    .ok()
                    .body(new ResponseDTO(HttpStatus.OK, "로그인 성공", tokenDTO)); // HttpStatus.OK 사용

        } catch (FailedLoginException e) {
            log.error("[UserController] Login Failed: {}", e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDTO(HttpStatus.UNAUTHORIZED, e.getMessage(), null));
        } catch (Exception e) {
            log.error("[UserController] Login Error: ", e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류 발생", null));
        }
    }

    @PostMapping("/users")
    public ResponseEntity<ResponseDTO> signup(@RequestBody UserDTO userDTO) {    // 회원 가입 정보를 받아 냄
        return ResponseEntity
                .ok()
                .body(new ResponseDTO(HttpStatus.CREATED, "회원가입 성공", userFacadeService.registerUser(userDTO)));
    }

    @PutMapping("/profile")
    public ResponseEntity<ResponseDTO> modifyUsers(@RequestBody UserDTO userDTO) {
        return ResponseEntity
                .ok()
                .body(new ResponseDTO(HttpStatus.OK, "회원정보 수정 성공", userFacadeService.updateUser(userDTO)));
    }

    @PatchMapping("/{user_id}/leave")
    public ResponseEntity<ResponseDTO> deleteUsers(@RequestBody UserDTO userDTO) {
        // This method returns void, so you just call it
        userFacadeService.withdrawUser(userDTO);

        // Create a ResponseDTO, explicitly passing null for the data field
        return ResponseEntity
                .ok()
                .body(new ResponseDTO(HttpStatus.OK, "회원 탈퇴 성공", null));
    }
    /**
     * 현재 로그인된 사용자의 정보를 반환하는 엔드포인트
     * 프론트엔드 Layout.jsx에서 사용자의 권한 등을 확인하기 위해 호출됩니다.
     */
    @GetMapping("/me") // 사용자 정보 조회 엔드포인트
    @PreAuthorize("isAuthenticated()") // 이 엔드포인트는 인증된(로그인된) 사용자만 접근 가능하도록 설정
    public ResponseEntity<ResponseDTO> getMyUserInfo () {
        // SecurityContextHolder에서 현재 인증된 사용자의 Authentication 객체를 가져옴.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Authentication 객체의 principal에서 AuthDetails를 가져옴.
        // AuthDetails는 UserDetails를 구현하며, LoginUserDTO를 포함.
        if (authentication != null && authentication.getPrincipal() instanceof AuthDetails) {
            AuthDetails authDetails = (AuthDetails) authentication.getPrincipal();
            LoginUserDTO loginUserDTO = authDetails.getLoginUserDTO();

            // LoginUserDTO에 사용자의 역할(ROLE_ADMIN, ROLE_USER 등) 정보가 포함되어 있어야 함.
            // ResponseDTO에 LoginUserDTO를 담아 반환합니다.
            return ResponseEntity
                    .ok()
                    .body(new ResponseDTO(HttpStatus.OK, "사용자 정보 로드 성공", loginUserDTO));
        } else {
            // 인증 정보가 없거나, 예상치 못한 Principal 타입인 경우
            // 이 경우는 @PreAuthorize("isAuthenticated()") 때문에 발생하지 않아야 하지만, 안전을 위해 처리.
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(new ResponseDTO(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다.", null));
        }
    }
}