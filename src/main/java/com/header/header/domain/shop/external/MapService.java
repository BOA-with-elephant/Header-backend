package com.header.header.domain.shop.external;

import com.header.header.domain.shop.dto.MapServiceDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class MapService {

    /* 카카오맵 API에서 REST 방식으로 정보를 받아오기 위한 클래스*/

    private String REST_API_KEY;
    private final WebClient webClient;
                                                //yml에 api 코드 추가 후 @Value로 가져옴
    public MapService(WebClient.Builder webClientBuilder, @Value("${kakao.api-key.rest}") String REST_API_KEY ) {
        this.webClient = webClientBuilder.baseUrl("https://dapi.kakao.com").build();
        this.REST_API_KEY = REST_API_KEY;
    }

    //WebClient를 통해 사용자가 입력한 주소(address)를 기준으로 위도, 경도 값이 담긴 문서와 통신
    public Mono<MapServiceDTO> getCoordinates(String address){
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v2/local/search/address.json")
                        .queryParam("query", address)
                        .build())
                .header("Authorization", "KakaoAK " + REST_API_KEY)
                .retrieve()
                .bodyToMono(MapServiceDTO.class);
    }

}
