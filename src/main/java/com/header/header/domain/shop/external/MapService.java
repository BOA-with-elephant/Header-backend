package com.header.header.domain.shop.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class MapService {

    //yml에 api 코드 추가 후 @Value로 가져옴
    @Value("${kakao.api-key.rest}")
    private String REST_API_KEY;

    public Map<String, Double> getCoordinatesFromAddress(String address) {
        RestTemplate restTemplate = new RestTemplate();

        String url = "https://dapi.kakao.com/v2/local/search/address.json?query=" + UriUtils.encode(address, StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + REST_API_KEY);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.getBody());
            JsonNode documents = root.path("documents");
            if (documents.isArray() && documents.size() > 0) {
                JsonNode location = documents.get(0);
                double latitude = location.get("y").asDouble();
                double longitude = location.get("x").asDouble();

                Map<String, Double> result = new HashMap<>();
                result.put("shopLa", latitude);
                result.put("shopLong", longitude);
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
