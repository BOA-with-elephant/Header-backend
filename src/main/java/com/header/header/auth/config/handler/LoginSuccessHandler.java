package com.header.header.auth.config.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.header.header.auth.config.JwtTokenProvider;
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
 * 로그인 성공 시 JWT 토큰을 생성하여 응답에 포함시키는 핸들러입니다.
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
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        // JWT 토큰을 생성합니다.
        String jwtToken = jwtTokenProvider.generateToken(authentication);

        // 응답 본문에 포함할 데이터를 구성합니다.
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "로그인 성공");
        responseBody.put("token", jwtToken); // 생성된 JWT 토큰을 응답에 포함

        // 응답 헤더 설정
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(HttpServletResponse.SC_OK); // HTTP 상태 코드 200 OK

        // 응답 본문에 JSON 데이터를 작성합니다.
        objectMapper.writeValue(response.getWriter(), responseBody);

        // 클라이언트가 JWT를 쿠키에 저장하도록 하려면 다음 라인을 활성화하고,
        // HttpOnly 및 Secure 설정을 추가하여 보안을 강화할 수 있습니다.
        // Cookie jwtCookie = new Cookie("jwt_token", jwtToken);
        // jwtCookie.setHttpOnly(true); // JavaScript 접근 방지
        // jwtCookie.setSecure(true); // HTTPS 에서만 전송
        // jwtCookie.setPath("/"); // 모든 경로에서 쿠키 사용 가능
        // jwtCookie.setMaxAge((int) (jwtTokenProvider.getExpiration() / 1000)); // 만료 시간 설정 (초 단위)
        // response.addCookie(jwtCookie);
    }
}
