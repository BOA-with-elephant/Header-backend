package com.header.header.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final long MAX_AGE_SECS = 3600;  // 3600 -> 1시간동안 캐시하겠다는 의미

    @Override
    public void addCorsMappings(CorsRegistry registry){
        // 모든 경로에 대해
        registry.addMapping("/**")
                // Origin이 http:localhost:3000에 대해
                .allowedOrigins("https://your-app.vercel.app", "http://localhost:3000")
                // GET, POST, PUT, PATCH, DELETE 메소드를 허용
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE")
                // 클라이언트가 요청할 때 어떤 헤더를 포함해도 허용(Authrization, Content-Type, X-Custom-Header 등)
                // *는 모든 헤더를 허용. 하지만 보안상 민감한 API 에서는 명시적으로 필요한 헤더만 지정하는게 더 안전할 수 있다.
                .allowedHeaders("*")
                // 클라이언트가 쿠키, 인증 정보, 세션 등 자격 증명(credential)을 포함할 수 있도록 허용
                // true로 설정하면 브라우저가 withCredentials:true 옵션을 사용할 수 있다.
                // 하지만 이걸 설정하면 allowedOrigins("*")는 사용할 수 없고 Origin을 반드시 명시적으로 지정해야 한다.
                .allowCredentials(true)
                // 브라우저가 CORS preflight 요청의 결과를 캐시하는 시간 설정
                // Preflight 요청은 OPTIONS 메소드로 먼저 서버에 "이 요청 해도 돼?" 라고 물어보는 것이고, 이 설정은 그 응답을 얼마나 오래 기억할지를 결정함.
                .maxAge(MAX_AGE_SECS);
    }
}
