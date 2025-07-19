package com.header.header.auth.config;

import com.header.header.auth.model.AuthDetails;
import com.header.header.auth.model.dto.LoginUserDTO;
import com.header.header.auth.model.dto.TokenDTO;
import com.header.header.auth.model.service.AuthUserService;
import com.header.header.domain.user.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JWT 토큰 생성, 유효성 검증, 정보 추출을 담당하는 유틸리티 클래스입니다.
 */
@Component
public class JwtTokenProvider {

    private final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    private final Key key;
    private final long expiration;

    // JWT 토큰에 권한 정보를 담을 클레임의 키
    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";
    private final AuthUserService authUserService;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.token-validity-in-milliseconds}") long tokenValidityInMilliseconds, AuthUserService authUserService) {
        // 시크릿 키를 Base64 디코딩하여 Key 객체 생성
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = tokenValidityInMilliseconds;
        this.authUserService = authUserService;
    }

    public TokenDTO generateTokenDTO(LoginUserDTO loginUserDTO) {
        log.info("[TokenProvider] generateTokenDTO() Start");

        long now = System.currentTimeMillis();
        Date accessTokenExpiresIn = new Date(now + expiration);

        // LoginUserDTO의 isAdmin 필드를 기반으로 권한 정보를 가져옵니다.
        // isAdmin 필드를 통해 "ROLE_USER" 또는 "ROLE_ADMIN" 권한을 부여합니다.
        String authorities = loginUserDTO.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER";

        // JWT 토큰 생성
        String accessToken = Jwts.builder()
                // 토큰의 주체 (사용자 ID) 설정
                .setSubject(loginUserDTO.getUserId())
                // 권한 정보를 클레임으로 추가
                .claim(AUTHORITIES_KEY, authorities)
                // 토큰 만료 시간 설정
                .setExpiration(accessTokenExpiresIn)
                // 서명에 사용할 키와 알고리즘
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        System.out.println("조립된 accessToken 확인 = " + accessToken);

        log.info("[TokenProvider] generateTokenDTO() End");

        // TokenDTO 생성 시 사용자 이름(userId)과 토큰 만료 시간을 사용합니다.
        // loginUserDTO.getUserId()를 사용하여 사용자 ID를 직접 전달합니다.
        return new TokenDTO(BEARER_TYPE, loginUserDTO.getUserId(), accessToken, accessTokenExpiresIn.getTime());
    }

    /**
     * 주어진 JWT 토큰의 유효성을 검증합니다.
     * @param token 검증할 JWT 토큰
     * @return 토큰이 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true; // 토큰이 유효함
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            // 잘못된 JWT 서명입니다.
            System.out.println("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            // 만료된 JWT 토큰입니다.
            System.out.println("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            // 지원되지 않는 JWT 토큰입니다.
            System.out.println("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            // JWT 토큰이 잘못되었습니다.
            System.out.println("JWT 토큰이 잘못되었습니다.");
        }
        return false; // 토큰이 유효하지 않음
    }

    /**
     * JWT 토큰으로부터 인증 정보를 추출합니다.
     * @param token JWT 토큰
     * @return 인증 객체 (Authentication)
     */
    public Authentication getAuthentication(String token) {
        // 토큰에서 클레임(Claims) 정보를 파싱합니다.
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        UserDetails principal = authUserService.loadUserByUsername(claims.getSubject());

        // UsernamePasswordAuthenticationToken을 반환하여 SecurityContext에 저장
        // principal (UserDetails), credentials (비밀번호, 여기서는 빈 문자열), authorities
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }
}
