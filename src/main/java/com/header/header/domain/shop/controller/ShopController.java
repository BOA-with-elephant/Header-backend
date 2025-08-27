package com.header.header.domain.shop.controller;

import com.header.header.auth.model.AuthDetails;
import com.header.header.common.dto.response.ShopApiResponse;
import com.header.header.domain.reservation.dto.UserReservationDTO;
import com.header.header.domain.reservation.projection.UserReservationDetail;
import com.header.header.domain.reservation.service.UserReservationService;
import com.header.header.domain.shop.common.GetUserInfoByAuthDetails;
import com.header.header.common.dto.response.ResponseMessage;
import com.header.header.domain.shop.dto.ShopWithMenusSummaryDTO;
import com.header.header.domain.shop.projection.ShopDetailResponse;
import com.header.header.domain.shop.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "api/v1/shops", produces = "application/json; charset=UTF-8") // 사용자가 샵을 조회, 상세 조회, 예약 추가할 때
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class ShopController {

    private final ShopService shopService;

    private final UserReservationService userReservationService;

    private final GetUserInfoByAuthDetails getUserInfoByAuthDetails;

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

        Page<ShopWithMenusSummaryDTO> shopsWithPaging
                = shopService.findShopsByCondition(
                latitude,
                longitude,
                category,
                keyword,
                pageable
        );

        return ShopApiResponse.success("조회 성공", "shops", shopsWithPaging.getContent());
    }

    /*샵 상세조회*/
    @GetMapping("/{shopCode}")
    public ResponseEntity<ResponseMessage> selectShopsDetail(
            @PathVariable Integer shopCode) {

        List<ShopDetailResponse> shopDetail
                = shopService.readShopDetailByShopCode(shopCode);

        return ShopApiResponse.success("조회 성공", "shop-detail", shopDetail);
    }

    /*Post 새로운 예약 생성 */
    @PostMapping("/{shopCode}")
    public ResponseEntity<ResponseMessage> createReservation(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Integer shopCode,
            @RequestBody @Valid UserReservationDTO dto){

        Integer userCode = getUserInfoByAuthDetails.getUserCodeByAuthDetails(authDetails);
        dto.setUserCode(userCode);

        Optional<UserReservationDetail> reservationResult
                = userReservationService.createReservation(shopCode, dto);

        return ShopApiResponse.success("리소스 생성 성공", "reservation-result", reservationResult);
    }

}