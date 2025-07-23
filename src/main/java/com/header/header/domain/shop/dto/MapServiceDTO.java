package com.header.header.domain.shop.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MapServiceDTO {

    // 사용자가 입력한 주소 정보를 위도와 경도로 반환한 값을 담기 위한 DTO

    @JsonProperty("documents")
    private List<Document> documents;

    @Getter
    @NoArgsConstructor
    public static class Document {
        @JsonProperty("x") //경도
        private double longitude;
        @JsonProperty("y") //위도
        private double latitude;
    }
}
