package com.header.header.domain.shop.controller;

import com.header.header.domain.reservation.dto.UserReservationDTO;
import com.header.header.domain.reservation.dto.UserReservationSearchConditionDTO;
import com.header.header.domain.reservation.projection.UserReservationDetail;
import com.header.header.domain.reservation.projection.UserReservationSummary;
import com.header.header.domain.reservation.service.UserReservationService;
import com.header.header.domain.shop.common.ResponseMessage;
import com.header.header.domain.shop.dto.ShopSummaryResponseDTO;
import com.header.header.domain.shop.dto.ShopUserCodeDTO;
import com.header.header.domain.shop.projection.ShopDetailResponse;
import com.header.header.domain.shop.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/shops")
@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class})
// spring 보안 임시 해제
public class ShopController {

    // url상에서 데이터를 전달하는 경우(form 태그 등) @RequestParam 을 이용하고,
    //그 외의 경우 @RequestBody 를 이용하자!

    private final ShopService shopService;

    private final UserReservationService userReservationService;

    /*🐭 전체 샵 조회 (거리순, 이름/위치 조회*/
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

        Page<ShopSummaryResponseDTO> shopsWithPaging
                = shopService.findShopsByCondition(
                latitude,
                longitude,
                category,
                keyword,
                pageable
        );

        // 응답 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 응답 데이터 설정
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("shops", shopsWithPaging.getContent());

        ResponseMessage responseMessage = new ResponseMessage(200, "조회 성공", responseMap);

        return new ResponseEntity<>(responseMessage, headers, HttpStatus.OK);
    }

    /*🐭 샵 상세조회*/
    @GetMapping("/{shopCode}")
    public ResponseEntity<ResponseMessage> selectShopsDetail(
            @PathVariable Integer shopCode) {
        List<ShopDetailResponse> shopDetail
                = shopService.readShopDetailByShopCode(shopCode);

        // 응답 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 응답 데이터 설정
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("shop-detail", shopDetail);

        ResponseMessage responseMessage = new ResponseMessage(200, "조회 성공", responseMap);

        return new ResponseEntity<>(responseMessage, headers, HttpStatus.OK);

    }

    /*🐭 Post 새로운 예약 생성 */
    @PostMapping("/{shopCode}")
    public ModelAndView createReservation(
            @PathVariable Integer shopCode,
            @RequestBody @Valid UserReservationDTO dto,
            ModelAndView mv ){

        Optional<UserReservationDetail> reservationResult
                = userReservationService.createReservation(shopCode, dto);
        // 응답 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 응답 데이터 설정
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("reservation-result", reservationResult);

        ResponseMessage responseMessage = new ResponseMessage(201, "리소스 생성 성공", responseMap);

        mv.addObject(responseMessage);
        mv.setViewName("redirect:/shops/{shopCode}"); //반환할 뷰

        return mv;
    }

    /*🐭 회원이 자신이 예약한 내역 전체 목록 조회 (기간 필터)*/
    @GetMapping("/reservation")
    public ResponseEntity<ResponseMessage> selectReservations(
            @RequestBody @Valid UserReservationSearchConditionDTO condition
    ) {
        List<UserReservationSummary> reservationSummaryList
                = userReservationService.findResvSummaryByUserCode(condition);

        // 응답 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 응답 데이터 설정
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("shop-detail", reservationSummaryList);

        ResponseMessage responseMessage = new ResponseMessage(200, "조회 성공", responseMap);

        return new ResponseEntity<>(responseMessage, headers, HttpStatus.OK);
    }

    /*특정 예약 내역을 상세조회할 경우*/
    @GetMapping("reservation/{resvCode}")
    public ResponseEntity<ResponseMessage> getReservationDetail(
            @RequestBody ShopUserCodeDTO userCodeDTO,
            @PathVariable Integer resvCode
    ) {

        Integer userCode = userCodeDTO.getUserCode();

        Optional<UserReservationDetail> resvDetail
                = userReservationService.readDetailByUserCodeAndResvCode(userCode, resvCode);

        // 응답 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 응답 데이터 설정
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("shop-detail", resvDetail);

        ResponseMessage responseMessage = new ResponseMessage(200, "조회 성공", responseMap);

        return new ResponseEntity<>(responseMessage, headers, HttpStatus.OK);
    }

    /*예약을 취소할 경우*/
    @PatchMapping("reservation/{resvCode}")
    public ModelAndView cancelReservation(
            @RequestBody ShopUserCodeDTO userCodeDTO,
            @PathVariable Integer resvCode,
            ModelAndView mv
    ) {

        Integer userCode = userCodeDTO.getUserCode();

        userReservationService.cancelReservation(userCode, resvCode);
        // 취소는 정상적으로 되는데 서버 응답이 안됨...해결 요함

        // 응답 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 응답 데이터 설정
        Map<String, Object> responseMap = new HashMap<>();
        String message = "예약 정상 취소";
        responseMap.put("cancel", message);

        ResponseMessage responseMessage = new ResponseMessage(201, "리소스 생성 성공", responseMap);

        mv.addObject(responseMessage);
        mv.setViewName("redirect:/shops/reservation"); //반환할 뷰

        return mv;
//        return new ResponseEntity<>(responseMessage, headers, HttpStatus.OK);
    }

}