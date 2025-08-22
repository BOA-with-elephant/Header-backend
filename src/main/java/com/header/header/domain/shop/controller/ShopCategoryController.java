package com.header.header.domain.shop.controller;

import com.header.header.domain.shop.common.ResponseMessage;
import com.header.header.domain.shop.dto.ShopAndMenuCategoryDTO;
import com.header.header.domain.shop.dto.ShopCategoryDTO;
import com.header.header.domain.shop.service.ShopCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("api/v1/shops")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"},
        allowedHeaders = "*",
        methods = RequestMethod.GET)
public class ShopCategoryController {

    private final ShopCategoryService shopCategoryService;

    @GetMapping("/categories")
    public ResponseEntity<ResponseMessage> getShopCategories() {

        List<ShopCategoryDTO> shopCategories = shopCategoryService.findAllCategories();
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("shop-categories", shopCategories);

        // 응답 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseMessage responseMessage = new ResponseMessage(200, "조회 성공", responseMap);

        return new ResponseEntity<>(responseMessage, headers, HttpStatus.OK);
    }

    @GetMapping("/shop-menu-categories")
    public ResponseEntity<ResponseMessage> getShopCategoryAndMenu() {
        ShopAndMenuCategoryDTO shopAndMenuCategories = shopCategoryService.findAllShopAndMenuCategories();

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("categories", shopAndMenuCategories);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return ResponseEntity.ok().body(new ResponseMessage(200, "조회 성공", responseMap));
    }
}
