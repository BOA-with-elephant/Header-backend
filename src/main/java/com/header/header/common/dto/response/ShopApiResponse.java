package com.header.header.common.dto.response;

import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ShopApiResponse {

    /**
     * 데이터(Key-Value)를 포함하는 성공 응답을 생성합니다.
     * @param message 응답 메시지 (예: "조회 성공")
     * @param dataKey 응답 데이터의 키 (예: "shops")
     * @param dataValue 응답 데이터의 값
     * @return 생성된 ResponseEntity<ResponseMessage> 객체
     */
    public static ResponseEntity<ResponseMessage> success(String message, String dataKey, Object dataValue) {

        Map<String, Object> data = new HashMap<>();
        data.put(dataKey, dataValue);

        ResponseMessage responseMessage = new ResponseMessage(200, message, data);

        return ResponseEntity.ok(responseMessage);
    }

    /**
     * 삭제 성공 응답을 생성할 때 사용
     * @param message 응답 메시지 (예: "삭제 성공")
     * @return 생성된 ResponseEntity<ResponseMessage> 객체
     */
    public static ResponseEntity<ResponseMessage> delete(String message) {
        ResponseMessage responseMessage = new ResponseMessage(204, message, null);
        return ResponseEntity.ok(responseMessage);
    }
}
