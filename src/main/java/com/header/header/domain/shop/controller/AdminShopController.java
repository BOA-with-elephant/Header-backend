package com.header.header.domain.shop.controller;

import com.header.header.domain.shop.common.ResponseMessage;
import com.header.header.domain.shop.dto.ShopCreationDTO;
import com.header.header.domain.shop.dto.ShopDTO;
import com.header.header.domain.shop.dto.ShopUpdateDTO;
import com.header.header.domain.shop.projection.ShopDetailResponse;
import com.header.header.domain.shop.projection.ShopSummary;
import com.header.header.domain.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/myshop")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class AdminShopController {

    private final ShopService shopService;

    // 샵 생성
    @PostMapping("")
    public ResponseEntity<ResponseMessage> createShop(
            @RequestBody ShopCreationDTO dto
            ) {
        ShopDTO shopDTO = shopService.createShop(dto);
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("created-shop", shopDTO);

        return ResponseEntity.ok().body(new ResponseMessage(201, "리소스 생성 성공", responseMap));
    }

    // 보유한 샵 간략 조회
    @GetMapping("")
    public ResponseEntity<ResponseMessage> readShopSummaryByAdminCode(
            @RequestParam Integer adminCode
    ) {
        List<ShopSummary> shopList = shopService.readShopSummaryByAdminCode(adminCode);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("shop-list", shopList);

        return ResponseEntity.ok().body(new ResponseMessage(200, "조회 성공", responseMap));
    }

    // 샵 상세정보 조회
    @GetMapping("/{shopCode}")
    public ResponseEntity<ResponseMessage> readShopDetailByShopCode(
            @PathVariable Integer shopCode
    ) {
        List<ShopDetailResponse> shopDetail = shopService.readShopDetailByShopCode(shopCode);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("shop-detail", shopDetail);

        return ResponseEntity.ok().body(new ResponseMessage(200, "조회 성공", responseMap));
    }

    // 샵 정보 수정
    @PutMapping("/{shopCode}")
    public ResponseEntity<ResponseMessage> updateShop(
            @RequestParam Integer adminCode,
            @PathVariable Integer shopCode,
            @RequestBody ShopUpdateDTO dto
    ){
        ShopDTO updatedShop = shopService.updateShop(adminCode, shopCode, dto);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("updated-shop", updatedShop);

        return  ResponseEntity.ok().body(new ResponseMessage(201, "리소스 수정 성공", responseMap));
    }

    // 샵 비활성화
    @DeleteMapping("/{shopCode}")
    public ResponseEntity<ResponseMessage> deleteShop(
            @RequestParam Integer adminCode,
            @PathVariable Integer shopCode
    ){
        shopService.deActiveShop(adminCode, shopCode);

        return ResponseEntity.accepted().body(new ResponseMessage(204, "삭제 성공", null));
    }
}
