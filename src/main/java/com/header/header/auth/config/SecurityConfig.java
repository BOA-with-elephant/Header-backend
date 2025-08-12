package com.header.header.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.header.header.auth.config.handler.AuthFailHandler;
import com.header.header.auth.config.handler.LoginSuccessHandler;
import com.header.header.domain.shop.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

/**
 * Spring Security 설정을 담당하는 클래스입니다.
 * JWT 기반 인증을 위해 세션 관리를 비활성화하고 JWT 필터를 추가합니다.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private AuthFailHandler authFailHandler; // 로그인 실패 핸들러

    @Autowired
    private JwtTokenProvider jwtTokenProvider; // JWT 토큰 생성 및 검증 프로바이더

    @Autowired
    private ObjectMapper objectMapper; // JSON 직렬화를 위한 ObjectMapper (LoginSuccessHandler에서 사용)

    /**
     * 비밀번호 암호화를 위한 BCryptPasswordEncoder를 빈으로 등록합니다.
     * @return PasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    /**
     * 정적 리소스(CSS, JS, 이미지 등)에 대한 보안 필터 적용을 무시하도록 설정합니다.
     * @return WebSecurityCustomizer 인스턴스
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    /**
     * HTTP 보안 설정을 구성하는 SecurityFilterChain 빈입니다.
     * JWT 기반 인증을 위해 세션 관리를 STATELESS로 설정하고, JWT 필터를 추가하며,
     * 로그인 및 로그아웃 동작을 정의합니다.
     * @param http HttpSecurity 객체
     * @return SecurityFilterChain 인스턴스
     * @throws Exception 예외 발생 시
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ShopService shopService) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(withDefaults()) // CORS 설정은 그대로 유지 (예람님 코드에서 갖고 옴)
                // 세션 관리 전략을 STATELESS로 설정 (세션을 사용하지 않음)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 요청에 대한 권한 설정
                .authorizeHttpRequests(auth -> {
                    // 다음 경로들은 인증 없이 모든 사용자에게 허용
                    auth.requestMatchers("/auth/**", "/", "/main").permitAll();
                    // "/auth/session" (POST) 요청도 허용하여 로그인 시도를 가능하게 합니다.
                    auth.requestMatchers(HttpMethod.POST, "/auth/session").permitAll();
                    // "/my-shops/**" 경로는 ADMIN 역할만 접근 가능합니다.
                    auth.requestMatchers("/api/v1/my-shops/**").permitAll();
//                    auth.requestMatchers("/api/v1/myshops/**").hasRole("ADMIN");
                    // "/shops/**" 경로는 USER 역할, ADMIN역할 모두 접근 가능합니다.
                    auth.requestMatchers("/api/v1/shops/**").permitAll();
//                    auth.requestMatchers("/shops/**").hasAnyRole("ADMIN", "USER");
                    // 그 외 모든 요청은 인증된 사용자만 접근 가능합니다.
                    auth.anyRequest().authenticated();
                })
                // 폼 로그인 설정
                .formLogin(login -> {
                    login.loginPage("/auth/session"); // 로그인 페이지 URL
                    login.loginProcessingUrl("/auth/session"); // 로그인 처리 URL (POST 요청)
                    login.usernameParameter("userId");
                    login.passwordParameter("userPwd");
                    // 로그인 성공 시 LoginSuccessHandler를 사용합니다.
                    login.successHandler(new LoginSuccessHandler(jwtTokenProvider, objectMapper, shopService));
                    // 로그인 실패 시 AuthFailHandler를 사용합니다.
                    login.failureHandler(authFailHandler);
                })
                // 로그아웃 설정
                .logout(logout -> {
                    /*
                    logoutUrl()은 기본적으로 POST 요청을 처리하며,
                    사용금지(deprecated)된 AntPathRequestMatcher보다 더 간결하다.
                    Spring Security의 최신 권장 방식이다.
                    */
                    logout.logoutUrl("/auth/logout"); // 로그아웃 요청 URL (POST)
                    // JWT는 세션을 사용하지 않으므로 JSESSIONID 삭제나 세션 무효화는 필요 없다.
                    // 로그아웃 성공 시 200 OK 상태 코드를 반환
                    logout.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
                    logout.logoutSuccessUrl("/");
                })
                // 직접 작성한 커스텀 필터인 JwtAuthenticationFilter를 필터 체인에 추가 (이 필터는 모든 요청에서 JWT 토큰을 검증한다)
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)

                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NotNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:3000")
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }
}
