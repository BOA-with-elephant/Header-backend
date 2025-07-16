package com.header.header.domain.shop.controller;

import com.header.header.domain.reservation.dto.UserReservationDTO;
import com.header.header.domain.reservation.dto.UserReservationSearchConditionDTO;
import com.header.header.domain.reservation.projection.UserReservationDetail;
import com.header.header.domain.reservation.projection.UserReservationSummary;
import com.header.header.domain.reservation.service.UserReservationService;
import com.header.header.domain.shop.common.ErrorResponseMessage;
import com.header.header.domain.shop.common.ResponseMessage;
import com.header.header.domain.shop.dto.ShopSummaryResponseDTO;
import com.header.header.domain.shop.dto.ShopUserCodeDTO;
import com.header.header.domain.shop.enums.ShopErrorCode;
import com.header.header.domain.shop.exception.ShopExceptionHandler;
import com.header.header.domain.shop.projection.ShopDetailResponse;
import com.header.header.domain.shop.service.ShopService;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
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

import java.net.URI;
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

        try {
            Pageable pageable = PageRequest.of(page, 10);
            // 응답 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Page<ShopSummaryResponseDTO> shopsWithPaging
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

            ResponseMessage responseMessage = new ResponseMessage(200, "조회 성공", responseMap);

            return new ResponseEntity<>(responseMessage, headers, HttpStatus.OK);
        } catch (ShopExceptionHandler e) {

            // 최하단에 설정된 에러 코드용 메소드 리턴
            return getErrorCode(e);
        }
    }

    /*🐭 샵 상세조회*/
    @GetMapping("/{shopCode}")
    public ResponseEntity<ResponseMessage> selectShopsDetail(
            @PathVariable Integer shopCode) {

        try {
            // 응답 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            List<ShopDetailResponse> shopDetail
                    = shopService.readShopDetailByShopCode(shopCode);

            // 응답 데이터 설정
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("shop-detail", shopDetail);

            ResponseMessage responseMessage = new ResponseMessage(200, "조회 성공", responseMap);

            return new ResponseEntity<>(responseMessage, headers, HttpStatus.OK);
        } catch (ShopExceptionHandler e) {
            return getErrorCode(e);
        }
    }

    /*🐭 Post 새로운 예약 생성 */
    @PostMapping("/{shopCode}")
    public ResponseEntity<ResponseMessage> createReservation(
            @PathVariable Integer shopCode,
            @RequestBody @Valid UserReservationDTO dto,
            HttpServletRequest req){

            // HttpServletRequest에 있는 사용자가 이전에 있던 페이지
            String referer = req.getHeader("Referer");
            // HttpServletRequest에 담긴 정보와 함께 header 구성
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setLocation(URI.create(referer));
            // 응답 데이터 설정
            Map<String, Object> responseMap = new HashMap<>();

        try {
            Optional<UserReservationDetail> reservationResult
                    = userReservationService.createReservation(shopCode, dto);

            responseMap.put("reservation-result", reservationResult);
        } catch (ShopExceptionHandler e) {

        }
            ResponseMessage responseMessage = new ResponseMessage(201, "리소스 생성 성공", responseMap);
            return new ResponseEntity<>(responseMessage, headers, HttpStatus.CREATED);

    }

    /*🐭 회원이 자신이 예약한 내역 전체 목록 조회 (기간 필터)*/
    @GetMapping("/reservation")
    public ResponseEntity<ResponseMessage> selectReservations(
            @RequestBody @Valid UserReservationSearchConditionDTO condition
    ) {

        //시작 날짜, 종료 날짜 둘 다 있/없은 괜찮은데 둘 중 하나만 없으면 오류
        //프론트에서 시작날짜/종료날짜가 있어야 재요청 보낼 수 있도록 막는 작업 필요

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

    /*🐭 특정 예약 내역을 상세조회할 경우*/
    @GetMapping("reservation/{resvCode}")
    public ResponseEntity<ResponseMessage> getReservationDetail(
            @RequestBody ShopUserCodeDTO dto,
            @PathVariable Integer resvCode
    ) {

        Integer userCode = dto.getUserCode();

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

    /*🐭 예약을 취소할 경우*/
    @PatchMapping("reservation/{resvCode}")
    public ResponseEntity<ResponseMessage> cancelReservation(
            @RequestBody ShopUserCodeDTO userCodeDTO,
            @PathVariable Integer resvCode
    ) {

        Integer userCode = userCodeDTO.getUserCode();

        userReservationService.cancelReservation(userCode, resvCode);

        // 응답 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 응답 데이터 설정
        Map<String, Object> responseMap = new HashMap<>();
        String message = "예약 정상 취소";
        responseMap.put("cancel", message);

        ResponseMessage responseMessage = new ResponseMessage(204, "예약 취소 성공", responseMap);

        return new ResponseEntity<>(responseMessage, headers, HttpStatus.OK);
    }

    public ResponseEntity<ResponseMessage> getErrorCode(ShopExceptionHandler e) {
        // Custom 에러 정보를 담음
        ShopErrorCode errorInfo = e.getShopErrorCode();

        // 에러 정보에서 메시지와 안내 문구를 담음
        ErrorResponseMessage error = new ErrorResponseMessage(errorInfo.getCode(), errorInfo.getMessage());

        // 에러 정보를 Map에 담음
        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("error", error);

        // 정보를 담은 ResponseMessage 생성
        ResponseMessage responseMessage = new ResponseMessage(400, "조회 실패", responseMap);
        return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
    }

}