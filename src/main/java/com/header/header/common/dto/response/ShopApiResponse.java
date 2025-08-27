package com.header.header.common.dto.response;

import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

public class ShopApiResponse {

    /**
     * 생성 성공 응답을 생성할 때 사용
     * @return 생성된 ResponseEntity<ResponseMessage> 객체
     */
    public static ResponseEntity<ResponseMessage> create(String dataKey, Object dataValue) {

        Map<String, Object> data = new HashMap<>();
        data.put(dataKey, dataValue);

        ResponseMessage responseMessage = new ResponseMessage(201, "리소스 생성 성공", data);

        return ResponseEntity.ok(responseMessage);
    }

    public static ResponseEntity<ResponseMessage> read(String dataKey, Object dataValue) {

        Map<String, Object> data = new HashMap<>();
        data.put(dataKey, dataValue);

        ResponseMessage responseMessage = new ResponseMessage(200, "조회 성공", data);

        return ResponseEntity.ok(responseMessage);
    }

    public static ResponseEntity<ResponseMessage> update(String dataKey, Object dataValue) {

        Map<String, Object> data = new HashMap<>();
        data.put(dataKey, dataValue);

        ResponseMessage responseMessage = new ResponseMessage(201, "리소스 수정 성공", data);

        return ResponseEntity.ok(responseMessage);
    }

    /**
     * 삭제 성공 응답을 생성할 때 사용
     * @return 생성된 ResponseEntity<ResponseMessage> 객체
     */
    public static ResponseEntity<ResponseMessage> delete() {
        ResponseMessage responseMessage = new ResponseMessage(204, "삭제 성공", null);
        return ResponseEntity.ok(responseMessage);
    }
}
