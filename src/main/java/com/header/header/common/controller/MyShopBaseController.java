package com.header.header.common.controller;

import com.header.header.common.dto.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("api/v1/my-shops/{shopId}")
public abstract class MyShopBaseController {

    protected Integer shopId;

    @ModelAttribute
    public void setShopId(@PathVariable("shopId") Integer shopId){
        this.shopId = shopId;
    }

    /**
     * ApiResponse.success()와 ResponseEntity.ok()를 한번에 처리하는 헬퍼 메서드
     */
    protected  <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
