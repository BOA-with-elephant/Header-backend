package com.header.header.domain.shop.controller;

import com.header.header.domain.shop.common.ResponseMessage;
import com.header.header.domain.shop.dto.HolCreationDTO;
import com.header.header.domain.shop.dto.HolResDTO;
import com.header.header.domain.shop.dto.HolUpdateDTO;
import com.header.header.domain.shop.projection.ShopHolidayInfo;
import com.header.header.domain.shop.service.ShopHolidayService;
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
@RequestMapping("api/v1/my-shops")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE})
public class ShopHolidayController {

    private final ShopHolidayService shopHolidayService;

    /*각각의 샵이 가진 휴일 정보를 조회*/
    @GetMapping("/{shopCode}/holidays")
    public ResponseEntity<ResponseMessage> getShopHoliday(@PathVariable Integer shopCode) {
        List<ShopHolidayInfo> holidayList = shopHolidayService.getShopHolidayInfo(shopCode);

        // 응답 데이터 설정
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("holiday-list", holidayList);

        return ResponseEntity.ok().body(new ResponseMessage(200, "조회 성공", responseMap));
    }

    /* 신규 휴일 등록 */
    @PostMapping("/{shopCode}/holidays")
    public ResponseEntity<ResponseMessage> createShopHoliday(
            @PathVariable Integer shopCode,
            @RequestBody HolCreationDTO dto) { // dto에서 샵 삭제 후 shopCode는 path로 받기

        HolResDTO res = shopHolidayService.createShopHoliday(shopCode, dto);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("created-holiday", res);

        return ResponseEntity.ok().body(new ResponseMessage(201, "휴일 생성 성공", responseMap));
    }

    /* 휴일 정보 수정 */
    @PutMapping("/{shopCode}/holidays/{shopHolCode}")
    public ResponseEntity<ResponseMessage> updateShopHoliday(
            @PathVariable Integer shopCode,
            @PathVariable Integer shopHolCode,
            @RequestBody HolUpdateDTO dto
    ){
        HolResDTO res = shopHolidayService.updateShopHoliday(shopCode, shopHolCode, dto);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("updated-holiday", res);

        return ResponseEntity.ok().body(new ResponseMessage(201, "휴일 수정 성공", responseMap));
    }


    /*휴일 삭제*/
    @DeleteMapping("/{shopCode}/holidays/{shopHolCode}")
    public ResponseEntity<ResponseMessage> deleteShopHoliday(
            @PathVariable Integer shopCode,
            @PathVariable Integer shopHolCode
    ){
        shopHolidayService.deleteShopHoliday(shopCode, shopHolCode);

        return ResponseEntity.ok().body(new ResponseMessage(204, "휴일 삭제 성공", null));
    }


}
