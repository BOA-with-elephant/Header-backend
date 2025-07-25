package com.header.header.domain.reservation.controller;

import com.header.header.auth.model.AuthDetails;
import com.header.header.domain.reservation.dto.ReservationDateAndTimeDTO;
import com.header.header.domain.reservation.dto.UserReservationSearchConditionDTO;
import com.header.header.domain.reservation.dto.UserResvAvailableScheduleDTO;
import com.header.header.domain.reservation.projection.UserReservationDetail;
import com.header.header.domain.reservation.projection.UserReservationSummary;
import com.header.header.domain.reservation.service.UserReservationService;
import com.header.header.domain.shop.common.GetUserInfoByAuthDetails;
import com.header.header.domain.shop.common.ResponseMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("api/v1/shops/reservation")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class UserReservationController {

    private final UserReservationService userReservationService;
    private final GetUserInfoByAuthDetails getUserInfoByAuthDetails;

    /*회원이 자신이 예약한 내역 전체 목록 조회 (기간 필터)*/
    @GetMapping("")
    public ResponseEntity<ResponseMessage> selectReservations(
            @AuthenticationPrincipal AuthDetails authDetails,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate
    ) {

        Integer userCode = getUserInfoByAuthDetails.getUserCodeByAuthDetails(authDetails);

        UserReservationSearchConditionDTO condition
                = new UserReservationSearchConditionDTO(userCode, startDate, endDate);

        List<UserReservationSummary> reservationSummaryList
                = userReservationService.findResvSummaryByUserCode(condition);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("resv-list", reservationSummaryList);

        return ResponseEntity.ok().body(new ResponseMessage(200, "조회 성공", responseMap));
    }

    /*특정 예약 내역을 상세조회할 경우*/
    @GetMapping("/{resvCode}")
    public ResponseEntity<ResponseMessage> getReservationDetail(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Integer resvCode
    ) {

        Integer userCode = getUserInfoByAuthDetails.getUserCodeByAuthDetails(authDetails);

        Optional<UserReservationDetail> resvDetail
                = userReservationService.readDetailByUserCodeAndResvCode(userCode, resvCode);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("resv-detail", resvDetail);

        return ResponseEntity.ok().body(new ResponseMessage(200, "조회 성공", responseMap));
    }

    /*예약을 취소할 경우*/
    @DeleteMapping("/{resvCode}")
    public ResponseEntity<ResponseMessage> cancelReservation(
            @AuthenticationPrincipal AuthDetails authDetails,
            @PathVariable Integer resvCode
    ) {
        Integer userCode = getUserInfoByAuthDetails.getUserCodeByAuthDetails(authDetails);

        userReservationService.cancelReservation(userCode, resvCode);

        return ResponseEntity.ok().body(
                new ResponseMessage(204, "삭제 성공", null));
    }

    // 가능한 시간을 선택, 추출하여 json 형식으로 보내줌
    @GetMapping("{shopCode}/available-schedule")
    public ResponseEntity<ResponseMessage> getAvailableSchedule(
            @PathVariable Integer shopCode
    ) {

        int dateRangeToGet = 30;

        List<UserResvAvailableScheduleDTO> scheduleList = userReservationService.getAvailableSchedule(shopCode, dateRangeToGet);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("schedule", scheduleList);

        return ResponseEntity.ok().body(
                new ResponseMessage(200, "조회 성공", responseMap)
        );
    }

    @GetMapping("{shopCode}/resv-time-and-date")
    public ResponseEntity<ResponseMessage> getResvDateAndTime(@PathVariable Integer shopCode) {

        int dateRangeToGet = 365;

        List<ReservationDateAndTimeDTO> dateAndTimeList = userReservationService.getReservationDateAndTime(shopCode, dateRangeToGet);

        Map<String, Object> responseMap = new HashMap<>();
        responseMap.put("schedule", dateAndTimeList);

        return ResponseEntity.ok().body(
                new ResponseMessage(200, "조회 성공", responseMap)
        );
    }
}
