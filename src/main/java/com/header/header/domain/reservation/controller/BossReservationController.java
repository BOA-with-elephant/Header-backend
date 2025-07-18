package com.header.header.domain.reservation.controller;

import com.header.header.domain.reservation.dto.BossReservationDTO;
import com.header.header.domain.reservation.dto.BossResvProjectionDTO;
import com.header.header.domain.reservation.service.BossReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.GET;

import java.sql.Date;
import java.util.List;

@RestController
@RequestMapping("/my-shops/{shopCode}/reservation")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class BossReservationController {

    private final BossReservationService bossReservationService;

    public BossReservationController(BossReservationService bossReservationService){
        this.bossReservationService = bossReservationService;
    }

    /* 당월 가게 에약 내역 조회 */
//    @GetMapping("/{date}")
//    public ResponseEntity<List<BossResvProjectionDTO>> findReservationListOfThisMonth(@PathVariable("shopCode") Integer shopCode, @PathVariable("date") String thisMonth){
//        try{
//            List<BossResvProjectionDTO> reservationList = bossReservationService.findReservationList(shopCode, thisMonth);
//
//            for(BossResvProjectionDTO list : reservationList){
//                System.out.println(list);
//            }
//            return ResponseEntity.ok(reservationList);
//        } catch (Exception e){
//            return ResponseEntity.internalServerError().build();
//        }
//    }

    /* 당월 가게 에약 내역 조회 */
    /* 검색 결과 조회 - 날짜별, 고객 이름별, 메뉴 이름 별 */
    @GetMapping("")
    public ResponseEntity<List<BossResvProjectionDTO>> searchResultReservationList(
            @PathVariable(value = "shopCode") Integer shopCode,
            @RequestParam(value = "date", required = false) String thisMonth,
            @RequestParam(value = "resvDate", required = false) String resvDate,
            @RequestParam(value = "userName", required = false) String userName,
            @RequestParam(value = "menuName", required = false) String menuName){

        try{
            List<BossResvProjectionDTO> result;

            if(thisMonth != null){
                result = bossReservationService.findReservationList(shopCode, thisMonth);
            } else if(resvDate != null){
                Date formattedDate = Date.valueOf(resvDate);
                result = bossReservationService.findReservationListByDate(shopCode, formattedDate);
            } else if(userName != null){
                result = bossReservationService.findReservationListByName(shopCode, userName);
            } else if(menuName != null){
                result = bossReservationService.findReservationListByMenuName(shopCode, menuName);
            } else {
                return ResponseEntity.badRequest().build();
            }

            return ResponseEntity.ok(result);
        } catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{resvCode}")
    public ResponseEntity<BossResvProjectionDTO> searchDetailReservationInfo(@PathVariable("shopCode") Integer shopCode, @PathVariable("resvCode") Integer resvCode){
        try {
            BossResvProjectionDTO result = bossReservationService.findReservationByResvCode(resvCode);

            return ResponseEntity.ok(result);
        } catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }
}
