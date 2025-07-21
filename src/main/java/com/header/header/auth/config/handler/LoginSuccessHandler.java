package com.header.header.auth.config.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.header.header.auth.config.JwtTokenProvider;
import com.header.header.auth.model.AuthDetails;
import com.header.header.auth.model.dto.LoginUserDTO;
import com.header.header.auth.model.dto.TokenDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 로그인 성공 시 JWT 토큰을 생성하여 응답에 포함시키는 핸들러.
 * 사용자 정보를 응답 본문에 포함하며, 특정 사용자 상태에 따른 메시지를 제공한다.
 */
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 생성 및 관리를 위한 프로바이더
    private final ObjectMapper objectMapper; // JSON 직렬화를 위한 ObjectMapper

    /**
     * LoginSuccessHandler의 생성자입니다.
     * @param jwtTokenProvider JWT 토큰 프로바이더
     * @param objectMapper JSON 객체 매퍼
     */
    public LoginSuccessHandler(JwtTokenProvider jwtTokenProvider, ObjectMapper objectMapper) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.objectMapper = objectMapper;
    }

    /**
     * 인증 성공 시 호출되는 메서드입니다.
     * JWT 토큰을 생성하고, 이를 응답 본문에 JSON 형태로 포함하여 클라이언트에 전송합니다.
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param authentication 인증 객체
     * @throws IOException 입출력 예외
     * @throws ServletException 서블릿 예외
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        // 응답 본문에 담을 데이터를 위한 Map 준비
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "로그인 성공입니다."); // 기본 성공 메시지 설정

        // 1. 인증된 사용자 정보 가져오기 (핵심!)
        // LoginUserDTO 변수를 여기서 바로 초기화하지 않고, 아래 if 블록 안에서 할당합니다.
        LoginUserDTO loginUserDTO = null;

        // Spring Security가 로그인에 성공하면, Authentication 객체 안에 UserDetails 타입의 Principal을 넣어줍니다.
        // 우리 프로젝트에서는 이 Principal이 바로 AuthDetails 객체입니다.
        if (authentication.getPrincipal() instanceof AuthDetails) {
            AuthDetails authDetails = (AuthDetails) authentication.getPrincipal();
            loginUserDTO = authDetails.getLoginUserDTO(); // AuthDetails에서 LoginUserDTO를 가져와 loginUserDTO에 할당
        }

        // 2. JWT 토큰 생성 및 사용자 정보 포함 로직
        // loginUserDTO가 성공적으로 가져와졌을 때만 (null이 아닐 때만) 토큰을 생성하고 사용자 정보를 추가합니다.
        if (loginUserDTO != null) {
            // 2.1. jwtTokenProvider.generateTokenDTO(loginUserDTO) 호출의 결과는 TokenDTO 객체입니다.
            TokenDTO tokenDto = jwtTokenProvider.generateTokenDTO(loginUserDTO);

            // 2.2. TokenDTO 객체에서 실제 JWT 문자열(accessToken)을 추출합니다.
            String accessToken = tokenDto.getAccessToken();

            // 2.3. 추출한 accessToken을 응답 본문에 담습니다.
            responseBody.put("token", accessToken);

            // 2.4. 사용자 정보 (LoginUserDTO) 응답 본문에 포함
            responseBody.put("userInfo", loginUserDTO);

            // 2.5. HTTP 응답 상태 코드 설정 (200 OK)
            response.setStatus(HttpServletResponse.SC_OK);

        } else {
            responseBody.put("message", "사용자 정보를 가져오는데 실패했습니다.");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        // 3. 공통 응답 헤더 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // 4. 응답 본문에 JSON 데이터 작성
        objectMapper.writeValue(response.getWriter(), responseBody);
    }
}
