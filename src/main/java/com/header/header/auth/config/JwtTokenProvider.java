package com.header.header.auth.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * JWT 토큰 생성, 유효성 검증, 정보 추출을 담당하는 유틸리티 클래스입니다.
 */
@Component
public class JwtTokenProvider {

    // application.properties 또는 application.yml에서 secret 키를 주입받는다.
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}") // JWT 유효 기간 (밀리초 단위)
    private long expiration;

    private Key key; // JWT 서명에 사용할 키

    /**
     * Secret 값을 사용하여 키를 초기화합니다.
     * 이 메서드는 빈 생성 후 자동으로 호출됩니다.
     */
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * 사용자 인증 정보를 기반으로 JWT 토큰을 생성합니다.
     * @param authentication 인증 객체
     * @return 생성된 JWT 토큰 문자열
     */
    public String generateToken(Authentication authentication) {
        // 인증된 사용자의 권한(Authority)들을 쉼표로 구분된 문자열로 변환합니다.
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime(); // 현재 시간
        Date validity = new Date(now + expiration); // 토큰 만료 시간 설정

        // JWT 토큰을 빌드하고 서명합니다.
        return Jwts.builder()
                .setSubject(authentication.getName()) // 토큰의 주체 (사용자 ID)
                .claim("auth", authorities) // 권한 정보를 클레임으로 추가
                .setExpiration(validity) // 토큰 만료 시간
                .signWith(key, SignatureAlgorithm.RS256) // 서명에 사용할 키와 알고리즘
                .compact(); // JWT를 압축하여 문자열로 반환
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

        // 클레임에서 권한 정보를 추출하여 GrantedAuthority 컬렉션으로 변환합니다.
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("auth").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // UserDetails 객체를 생성합니다. (여기서는 Spring Security의 User 클래스를 사용)
        UserDetails principal = new User(claims.getSubject(), "", authorities);

        // UsernamePasswordAuthenticationToken을 반환하여 SecurityContext에 저장할 수 있도록 합니다.
        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }
}
