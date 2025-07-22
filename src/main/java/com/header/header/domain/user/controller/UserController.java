package com.header.header.domain.user.controller;

import com.header.header.auth.model.dto.LoginUserDTO;
import com.header.header.auth.model.dto.SignupDTO;
import com.header.header.auth.model.dto.TokenDTO;
import com.header.header.auth.model.service.AuthUserService;
import com.header.header.common.dto.ResponseDTO;
import com.header.header.domain.user.dto.UserDTO;
import com.header.header.domain.user.service.UserFacadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.login.FailedLoginException;

@RestController
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
        // 로그인 성공 시 200 OK와 토큰 정보를 담은 ResponseDTO 반환
        return ResponseEntity
                .ok()
                .body(new ResponseDTO(HttpStatus.OK, "로그인 성공", userFacadeService.loginUser(loginUserDTO)));
    }

    @PostMapping("/users")
    public ResponseEntity<ResponseDTO> signup(@RequestBody UserDTO userDTO) {	// 회원 가입 정보를 받아 냄
        return ResponseEntity
                .ok()
                .body(new ResponseDTO(HttpStatus.CREATED, "회원가입 성공", userFacadeService.registerUser(userDTO)));
    }
}