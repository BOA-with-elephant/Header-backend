package com.header.header.domain.shop.external;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

@RequiredArgsConstructor
public class KakaoMapService {

    //yml에 api 코드 추가 후 @Value로 가져옴
    @Value("${kakao.api-key.rest}")
    private final String REST_API_KEY;


}
