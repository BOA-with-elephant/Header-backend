package com.header.header.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(){
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // holidays 캐시 설정
        cacheManager.registerCustomCache("holidays",
                Caffeine.newBuilder()
                        .expireAfterWrite(48, TimeUnit.HOURS) // 휴일 정보는 자주 바뀌지 않음
                        .maximumSize(3000) // 100개 가게의 30일치 휴일 정보 저장
                        .build());

        // reserved-schedule 캐시 설정
        cacheManager.registerCustomCache("reserved-schedule",
                Caffeine.newBuilder()
                        .expireAfterWrite(24, TimeUnit.HOURS) // 예약 정보는 매일 바뀜
                        .maximumSize(10000) // 영업 시간이 9~18 인 가게 40개의 예약 정보 저장
                        .build());

        return cacheManager;
    }

}
