package com.header.header.domain.shop.controller;

import com.header.header.domain.reservation.dto.UserReservationDTO;
import com.header.header.domain.reservation.dto.UserReservationSearchConditionDTO;
import com.header.header.domain.reservation.enums.UserReservationErrorCode;
import com.header.header.domain.reservation.exception.UserReservationExceptionHandler;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.net.URI;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
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
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH})
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
            return getShopErrorCode(e);
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
            return getShopErrorCode(e);
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

        try {
            Optional<UserReservationDetail> reservationResult
                    = userReservationService.createReservation(shopCode, dto);

            // 응답 데이터 설정
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("reservation-result", reservationResult);

            ResponseMessage responseMessage = new ResponseMessage(201, "리소스 생성 성공", responseMap);
            return new ResponseEntity<>(responseMessage, headers, HttpStatus.CREATED);

        } catch (ShopExceptionHandler e) {

            return getShopErrorCode(e);

        }
    }

    /*🐭 회원이 자신이 예약한 내역 전체 목록 조회 (기간 필터)*/
    @GetMapping("/reservation")
    public ResponseEntity<ResponseMessage> selectReservations(
            @RequestParam Integer userCode,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {

        try {//시작 날짜, 종료 날짜 둘 다 있/없은 괜찮은데 둘 중 하나만 없으면 오류
            //프론트에서 시작날짜/종료날짜가 있어야 재요청 보낼 수 있도록 막는 작업 필요

            UserReservationSearchConditionDTO condition
                    = new UserReservationSearchConditionDTO(userCode, startDate, endDate);

            log.info(condition.toString());

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
        } catch (ShopExceptionHandler e) {
            return getShopErrorCode(e);
        }
    }

    /*🐭 특정 예약 내역을 상세조회할 경우*/
    @GetMapping("reservation/{resvCode}")
    public ResponseEntity<ResponseMessage> getReservationDetail(
            ShopUserCodeDTO dto, //@RequestParam이 없어야 dto를 받고, 잘 받아옴
            @PathVariable Integer resvCode
    ) {

        Integer userCode = dto.getUserCode();

        try {
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
        } catch (ShopExceptionHandler e) {
            return getShopErrorCode(e);
        }
    }

    /*🐭 예약을 취소할 경우*/
    @DeleteMapping("reservation/{resvCode}")
    public ResponseEntity<ResponseMessage> cancelReservation(
            @RequestParam Integer userCode,
            @PathVariable Integer resvCode
    ) {

        /*log.info("userCode: {}, resvCode: {}", userCode, resvCode);

        try {// 응답 데이터 설정

            log.info("userCode 가져오기");
//            Integer userCode = userCodeDTO.getUserCode();
            log.info("예약취소 try 시작");
            userReservationService.cancelReservation(userCode, resvCode);
            // 응답 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("cancel", "예약 정상 취소");
            responseMap.put("resvCode", resvCode);
            responseMap.put("userCode", userCode);

            ResponseMessage responseMessage = new ResponseMessage(200, "예약 취소 성공", responseMap);

            return new ResponseEntity<>(responseMessage, headers, HttpStatus.OK);
        } catch (UserReservationExceptionHandler e) {
            log.error("UserReservationExceptionHandler 에러 발생");
            return getUserErrorCode(e);
        } catch (ShopExceptionHandler e) {
            log.error("ShopExceptionHandler 에러 발생");
            return getShopErrorCode(e);
        }*/

        // 오류 찾으려고 간소화 -> @PatchMapping -> @PutMapping으로 수정했더니 작동함
        userReservationService.cancelReservation(userCode, resvCode);

        return ResponseEntity.ok().body(
                new ResponseMessage(204, "삭제 성공", null));
    }

    public ResponseEntity<ResponseMessage> getShopErrorCode(ShopExceptionHandler e) {
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

    public ResponseEntity<ResponseMessage> getUserErrorCode(UserReservationExceptionHandler e) {
        UserReservationErrorCode errorInfo = e.getURErrorCode();
        ErrorResponseMessage error = new ErrorResponseMessage(errorInfo.getCode(), errorInfo.getMessage());

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("error", error);

        // 정보를 담은 ResponseMessage 생성
        ResponseMessage responseMessage = new ResponseMessage(400, "조회 실패", responseMap);
        return new ResponseEntity<>(responseMessage, HttpStatus.BAD_REQUEST);
    }

}