package com.header.header.domain.user.controller;

import com.header.header.auth.model.AuthDetails;
import com.header.header.auth.model.dto.LoginUserDTO;
import com.header.header.auth.model.dto.SignupDTO;
import com.header.header.auth.model.dto.TokenDTO;
import com.header.header.auth.model.service.AuthUserService;
import com.header.header.auth.model.service.EmailService;
import com.header.header.common.dto.ResponseDTO;
import com.header.header.domain.shop.dto.ShopDTO;
import com.header.header.domain.user.dto.UserDTO;
import com.header.header.domain.user.service.UserFacadeService;
import com.header.header.domain.user.service.UserService;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.FailedLoginException;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/auth")
public class UserController {

    @Autowired
    private final UserFacadeService userFacadeService;
    @Autowired
    private AuthUserService authUserService;
    @Autowired
    private final EmailService emailService;
    @Autowired
    private UserService userService;

    public UserController(UserFacadeService userFacadeService, EmailService emailService) {
        this.userFacadeService = userFacadeService;
        this.emailService = emailService; // The emailService parameter from the constructor is assigned to the emailService field.
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
                    .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8")
                    //.body(new ResponseDTO(HttpStatus.OK, "로그인 성공")); //tokenDTO에서 JWT 노출됨. 삭제필요
                    .body(new ResponseDTO(HttpStatus.OK, "로그인 성공", tokenDTO));

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
    public ResponseEntity<ResponseDTO> signup(@RequestBody SignupDTO signupDTO) {    // 회원 가입 정보를 받아 냄
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8")
                .body(new ResponseDTO(HttpStatus.CREATED, "회원가입 성공", userFacadeService.registerUser(signupDTO)));
    }

    @PutMapping("/profile")
    public ResponseEntity<ResponseDTO> modifyUsers(@RequestBody UserDTO userDTO, Authentication authentication) {
        AuthDetails authDetails = (AuthDetails) authentication.getPrincipal();
        LoginUserDTO loginUserDTO = authDetails.getLoginUserDTO();

        userDTO.setUserCode(loginUserDTO.getUserCode());

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8")
                .body(new ResponseDTO(HttpStatus.OK, "회원정보 수정 성공", userFacadeService.updateUser(userDTO)));
    }

    @PatchMapping("/auth/{user_id}/leave")
    public ResponseEntity<ResponseDTO> deleteUsers(@RequestBody UserDTO userDTO) {
        // This method returns void, so you just call it
        userFacadeService.withdrawUser(userDTO);

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8")
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
                    .body(new ResponseDTO(HttpStatus.OK,"사용자 정보 로드 성공",loginUserDTO));
        } else {
            // 인증 정보가 없거나, 예상치 못한 Principal 타입인 경우
            // 이 경우는 @PreAuthorize("isAuthenticated()") 때문에 발생하지 않아야 하지만, 안전을 위해 처리.
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8")
                    .body(new ResponseDTO(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다.", null));
        }
    }

    @PostMapping("/password-reset")
    public ResponseEntity<ResponseDTO> resetPassword(@RequestBody UserDTO userDTO) {
        userFacadeService.modifyPwd(userDTO); // 본인확인 및 다음 단계 준비
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8")
                .body(new ResponseDTO(HttpStatus.OK, "본인 확인 완료", null));
    }

    /**
     * 이메일로 인증번호를 발송하는 엔드포인트
     */
    @PostMapping("/verification-code")
    public ResponseEntity<ResponseDTO> sendVerificationCode(@RequestBody SignupDTO signupDTO, HttpSession httpSession){
        try{
            int authNumber = emailService.sendMail(signupDTO.getUserEmail());
            httpSession.setAttribute(signupDTO.getUserEmail(), String.valueOf(authNumber));
            // userId와 userEmail을 세션에 저장
            httpSession.setAttribute("userId", signupDTO.getUserId());
            httpSession.setAttribute("userEmail", signupDTO.getUserEmail());
            return ResponseEntity.ok()
                    .body(new ResponseDTO(HttpStatus.OK, "인증번호가 발송되었습니다.", null));
        }
        catch (Exception ex){
            log.error("인증코드 발급 실패", ex);
            return ResponseEntity.badRequest()
                    .body(new ResponseDTO(HttpStatus.BAD_REQUEST, "인증코드 발급이 실패하였습니다.", null));
        }
    }

    /**
     * 사용자가 입력한 인증번호를 검증하는 엔드포인트
     */
    @PostMapping("/verification-code/validate")
    public ResponseEntity<ResponseDTO> validateVerificationCode(@RequestBody Map<String, String> requestBody, HttpSession httpSession) {
        String userEmail = requestBody.get("userEmail");
        String verifyCode = requestBody.get("verifyCode");

        try {
            boolean isVerified = emailService.checkAuthNum(userEmail, verifyCode);
            if (isVerified) {
                httpSession.removeAttribute(userEmail); // 인증 성공 후 세션에서 제거
                return ResponseEntity.ok()
                        .body(new ResponseDTO(HttpStatus.OK, "인증이 성공적으로 완료되었습니다.", null));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ResponseDTO(HttpStatus.BAD_REQUEST, "인증번호가 올바르지 않습니다.", null));
            }
        } catch (Exception ex) {
            log.error("인증번호 확인 실패", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDTO(HttpStatus.INTERNAL_SERVER_ERROR, "인증 절차 중 오류가 발생했습니다.", null));
        }
    }

    /**
     * 세션에서 사용자 정보를 가져오는 엔드포인트
     */
    @GetMapping("/session/user-info")
    public ResponseEntity<Map<String, String>> getUserInfoFromSession(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        String userEmail = (String) session.getAttribute("userEmail");
        Map<String, String> userInfo = new HashMap<>();

        if (userId != null && userEmail != null) {
            userInfo.put("userId", userId);
            userInfo.put("userEmail", userEmail);
            return ResponseEntity.ok(userInfo);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}