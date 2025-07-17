package com.header.header.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.header.header.auth.config.handler.AuthFailHandler;
import com.header.header.auth.config.handler.LoginSuccessHandler;
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
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.security.config.Customizer.withDefaults;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 보호 비활성화 (JWT는 토큰 기반이므로 CSRF 공격에 덜 취약하며, 일반적으로 비활성화합니다)
                .csrf(csrf -> csrf.disable())

                // 세션 관리 전략을 STATELESS로 설정 (세션을 사용하지 않음)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 요청에 대한 권한 설정
                .authorizeHttpRequests(auth -> {
                    // 다음 경로들은 인증 없이 모든 사용자에게 허용합니다.
                    auth.requestMatchers("/auth/session", "/auth/users", "/auth/session/fail", "/", "/main").permitAll();
                    // "/public/**" 경로도 인증 없이 허용합니다.
                    auth.requestMatchers("/public/**").permitAll();
                    // "/auth/login" (POST) 요청도 허용하여 로그인 시도를 가능하게 합니다.
                    auth.requestMatchers(HttpMethod.POST, "/auth/login").permitAll();

                    // "/my-shops/**" 경로는 ADMIN 역할만 접근 가능합니다.
                    auth.requestMatchers("/my-shops/**").hasRole("ADMIN");
                    // "/shops/**" 경로는 USER 역할만 접근 가능합니다.
                    auth.requestMatchers("/shops/**").hasRole("USER");
                    // 그 외 모든 요청은 인증된 사용자만 접근 가능합니다.
                    auth.anyRequest().authenticated();
                })

                // 폼 로그인 설정
                .formLogin(login -> {
                    login.loginPage("/auth/session"); // 로그인 페이지 URL
                    login.loginProcessingUrl("/auth/login"); // 로그인 처리 URL (POST 요청)
                    login.usernameParameter("userId"); // 사용자 ID 파라미터 이름
                    login.passwordParameter("userPwd"); // 비밀번호 파라미터 이름
                    // 로그인 성공 시 LoginSuccessHandler를 사용합니다.
                    login.successHandler(new LoginSuccessHandler(jwtTokenProvider, objectMapper));
                    // 로그인 실패 시 AuthFailHandler를 사용합니다.
                    login.failureHandler(authFailHandler);
                })

                // 로그아웃 설정
                .logout(logout -> {
                    logout.logoutRequestMatcher(new AntPathRequestMatcher("/auth/logout", HttpMethod.POST.name())); // 로그아웃 요청 URL (POST)
                    // JWT는 세션을 사용하지 않으므로 JSESSIONID 삭제나 세션 무효화는 필요 없습니다.
                    // 만약 JWT를 HttpOnly 쿠키에 저장했다면 해당 쿠키를 삭제하는 로직을 추가할 수 있습니다.
                    logout.deleteCookies("jwt_token_cookie_name"); // 예시: JWT를 저장한 쿠키 이름
                    // 로그아웃 성공 시 200 OK 상태 코드를 반환합니다. (API 방식에 적합)
                    logout.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler());
                    // 또는 특정 URL로 리다이렉트: logout.logoutSuccessUrl("/");
                })

                // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 이전에 추가합니다.
                // 이 필터는 모든 요청에서 JWT 토큰을 검증합니다.
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(withDefaults())  // CORS 설정은 그대로 유지
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll() // ✅ 모든 요청 허용
                )
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable); // 필요시 비활성화

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
