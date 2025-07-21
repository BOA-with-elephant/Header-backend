package com.header.header.domain.shop.controller;

import com.header.header.domain.reservation.dto.UserReservationDTO;
import com.header.header.domain.reservation.projection.UserReservationDetail;
import com.header.header.domain.reservation.service.UserReservationService;
import com.header.header.domain.shop.common.ResponseMessage;
import com.header.header.domain.shop.projection.ShopDetailResponse;
import com.header.header.domain.shop.projection.ShopSearchSummaryResponse;
import com.header.header.domain.shop.service.ShopService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/shops")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class ShopController {

    private final ShopService shopService;

    private final UserReservationService userReservationService;

    /*전체 샵 조회 (거리순, 이름/위치 조회*/
    @GetMapping("")
    public ResponseEntity<ResponseMessage> selectShopsWithPaging(
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) String keyword,
            @RequestParam Integer page
    ) {

        // 기본 로딩 개수는 10개
            Pageable pageable = PageRequest.of(page, 10);

        Page<ShopSearchSummaryResponse> shopsWithPaging
                = shopService.findShopsByCondition(
                latitude,
                longitude,
                category,
                keyword,
                pageable
        );

        // 응답 데이터 설정
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("shops", shopsWithPaging.getContent());

        return ResponseEntity.ok().body(new ResponseMessage(200, "조회 성공", responseMap));
    }

    /*샵 상세조회*/
    @GetMapping("/{shopCode}")
    public ResponseEntity<ResponseMessage> selectShopsDetail(
            @PathVariable Integer shopCode) {

        List<ShopDetailResponse> shopDetail
                = shopService.readShopDetailByShopCode(shopCode);

        // 응답 데이터 설정
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("shop-detail", shopDetail);

        return ResponseEntity.ok().body(new ResponseMessage(200, "조회 성공", responseMap));
    }

    /*Post 새로운 예약 생성 */
    @PostMapping("/{shopCode}")
    public ResponseEntity<ResponseMessage> createReservation(
            @PathVariable Integer shopCode,
            @RequestBody @Valid UserReservationDTO dto,
            HttpServletRequest req){

        Optional<UserReservationDetail> reservationResult
                = userReservationService.createReservation(shopCode, dto);

        // 응답 데이터 설정
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("reservation-result", reservationResult);

        return ResponseEntity.ok().body(new ResponseMessage(201, "리소스 생성 성공", responseMap));
    }

}