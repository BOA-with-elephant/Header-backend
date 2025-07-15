package com.header.header.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/* COOLSMS API 연동을 위한 설정 파일 Bean으로 등록*/
@Configuration
@ConfigurationProperties(prefix = "coolsms.api")
@Data
public class CoolSmsApiConfig {
    private String apiKey;  // coolsms.api.api-key 와 매핑
    private String apiSecret; // coolsms.api.api-secret 와 매핑
    private String baseUrl; // coolsms.api.base-url 와 매핑

    @PostConstruct
    public void init(){
        validateConfig();

//        System.out.println("CoolSMS API 설정 로드 완료"); // comment. API 안될때 주석 풀고 확인해보세요.
    }

    // Key 설정 확인
    private void validateConfig(){
        if(apiKey == null || apiKey.trim().isEmpty()){
            throw new IllegalStateException("CoolSMS API Key가 설정되지 않았습니다.");
        }
        if(apiSecret == null || apiSecret.trim().isEmpty()){
            throw new IllegalStateException("CoolSMS API Secret이 설정되지 않았습니다.");
        }
        if(baseUrl == null || baseUrl.trim().isEmpty()){
            throw new IllegalStateException("CoolSMS Base Url이 설정되지 않았습니다.");
        }
    }
}
