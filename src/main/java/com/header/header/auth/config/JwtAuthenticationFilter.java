package com.header.header.auth.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


/**
 * JWT 토큰을 검증하고 SecurityContext에 인증 정보를 설정하는 필터입니다.
 * 모든 요청에 대해 한 번만 실행됩니다.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String AUTHORIZATION_HEADER = "Authorization"; // HTTP 헤더의 Authorization 키
    public static final String BEARER_PREFIX = "Bearer "; // JWT 토큰의 접두사

    private final JwtTokenProvider jwtTokenProvider; // JWT 토큰 관련 작업을 처리하는 프로바이더

    /**
     * JwtAuthenticationFilter의 생성자입니다.
     * @param jwtTokenProvider JWT 토큰 프로바이더
     */
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /**
     * 요청당 한 번 실행되는 필터 로직입니다.
     * HTTP 요청에서 JWT 토큰을 추출하고 유효성을 검증하여 인증 정보를 설정합니다.
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 예외
     * @throws IOException 입출력 예외
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // CORS 프리플라이트 요청(OPTIONS)은 토큰 검증 로직을 건너뛰고 바로 통과시킵니다.
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        String jwt = resolveToken(request); // 요청에서 JWT 토큰을 추출합니다.

        // 토큰이 존재하고 유효하면 인증 정보를 SecurityContext에 설정합니다.
        if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
            Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response); // 다음 필터로 요청을 전달합니다.
    }

    /**
     * HTTP 요청 헤더에서 JWT 토큰을 추출합니다.
     * "Authorization: Bearer <token>" 형식에서 <token> 부분을 가져옵니다.
     * @param request HTTP 요청 객체
     * @return 추출된 JWT 토큰 문자열 또는 null
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER); // Authorization 헤더 값을 가져옵니다.
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length()); // "Bearer " 접두사를 제거하고 토큰만 반환합니다.
        }
        return null; // 토큰이 없거나 형식이 올바르지 않으면 null 반환
    }
}