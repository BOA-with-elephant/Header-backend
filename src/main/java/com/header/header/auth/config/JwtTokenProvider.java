package com.header.header.auth.config;

import com.header.header.auth.model.AuthDetails;
import com.header.header.auth.model.dto.LoginUserDTO;
import com.header.header.auth.model.dto.TokenDTO;
import com.header.header.auth.model.service.AuthUserService;
import com.header.header.domain.shop.dto.ShopDTO;
import com.header.header.domain.user.service.UserService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
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
    private static final String AUTHORITIES_KEY = "role";
    private static final String BEARER_TYPE = "Bearer";
    private final UserService userService;

    public JwtTokenProvider(@Value("${jwt.secret}") String secret,
                            @Value("${jwt.expiration}") long tokenValidityTime,
                            @Lazy UserService userService) {
        // 시크릿 키를 Base64 디코딩하여 Key 객체 생성
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = tokenValidityTime;
        this.userService = userService;
    }

    public TokenDTO generateTokenDTO(LoginUserDTO loginUserDTO, List<ShopDTO> shopDTOs) {
        log.info("[TokenProvider] generateTokenDTO() Start");

        long now = System.currentTimeMillis();
        Date accessTokenExpiresIn = new Date(now + expiration);

        String authorities = loginUserDTO.isAdmin() ? "ROLE_ADMIN" : "ROLE_USER";

        // Extract all shop codes into a List<Integer>
        List<Integer> shopCodes = new ArrayList<>();
        if (shopDTOs != null && !shopDTOs.isEmpty()) {
            for (ShopDTO shopDTO : shopDTOs) {
                shopCodes.add(shopDTO.getShopCode());
            }
        }

        // JWT 빌더를 준비합니다.
        JwtBuilder jwtBuilder = Jwts.builder()
                .setSubject(loginUserDTO.getUserId())
                .claim(AUTHORITIES_KEY, authorities)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS512);

        // shopCodes가 비어있지 않은 경우에만 클레임에 추가합니다.
        if (!shopCodes.isEmpty()) {
            jwtBuilder.claim("shopCodes", shopCodes);
        }

        // JWT 토큰 생성
        String accessToken = jwtBuilder.compact();

        System.out.println("조립된 accessToken 확인 = " + accessToken);

        log.info("[TokenProvider] generateTokenDTO() End");

        // TokenDTO도 여러 개의 shopCode를 받을 수 있도록 수정해야 합니다.
        return new TokenDTO(BEARER_TYPE, loginUserDTO.getUserId(), shopCodes, accessToken, accessTokenExpiresIn.getTime());
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
        // 1. 클레임 파싱
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

        // 2. JWT에서 사용자 ID를 추출해 UserService로 사용자 정보 로드
        String userId = claims.getSubject();
        LoginUserDTO loginUserDTO = userService.findByUserId(userId);

        // 3. 사용자 존재 여부 확인 후 NullPointerException 방지
        if (loginUserDTO == null) {
            throw new UsernameNotFoundException("User not found from JWT subject: " + userId);
        }

        // 4. JWT에서 List<Integer> 타입의 shopCodes 클레임 추출
        // `get` 메소드의 두 번째 인자로 List.class를 사용하여 타입 캐스팅 오류를 방지합니다.
        List<Integer> shopCodes = claims.get("shopCodes", List.class);
        if (shopCodes != null) {
            // LoginUserDTO에 List<Integer>를 받을 수 있는 setShopCodes 메소드 추가 필요
            loginUserDTO.setShopCodes(shopCodes);
        }

        // 5. 업데이트된 DTO로 AuthDetails 객체 생성
        AuthDetails principal = new AuthDetails(loginUserDTO);

        // 6. 권한 목록 생성
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }
}
