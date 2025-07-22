package com.header.header.domain.reservation.controller;

import com.header.header.domain.reservation.dto.BossReservationDTO;
import com.header.header.domain.reservation.dto.BossResvInputDTO;
import com.header.header.domain.reservation.dto.BossResvProjectionDTO;
import com.header.header.domain.reservation.service.BossReservationService;
import com.header.header.domain.shop.common.ResponseMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import retrofit2.http.GET;
import retrofit2.http.Path;

import java.sql.Date;
import java.sql.Time;
import java.util.List;
import java.util.Map;

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

    @PostMapping(value="", produces = "application/json")
    public ResponseEntity<?> registNewReservation(@PathVariable("shopCode") Integer shopCode, @RequestBody Map<String, String> newReservationInfo){
        try{
            Date resvDate = Date.valueOf(newReservationInfo.get("resvDate"));
            Time resvTime = Time.valueOf(newReservationInfo.get("resvTime"));

            BossResvInputDTO inputDTO = new BossResvInputDTO();
            inputDTO.setUserName(newReservationInfo.get("userName"));
            inputDTO.setUserPhone(newReservationInfo.get("userPhone"));
            inputDTO.setResvDate(resvDate);
            inputDTO.setResvTime(resvTime);
            inputDTO.setMenuName(newReservationInfo.get("menuName"));
            inputDTO.setUserComment(newReservationInfo.get("userComment"));

           bossReservationService.registNewReservation(inputDTO, shopCode);
            System.out.println("넘어온 값 : " + inputDTO);

            ResponseEntity<?> response = ResponseEntity.ok(Map.of("message", "등록 완료"));
            System.out.println("Response headers : " + response.getHeaders());
           return response;
        } catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(400)
                    .header("Content-Type", "application/json")
                    .body(Map.of("message", "등록 실패"));
        }
    }

    @PutMapping(value = "/{resvCode}", produces = "application/json")
    public ResponseEntity<ResponseMessage> updateResvInfo(@PathVariable("shopCode") Integer shopCode, @PathVariable("resvCode") Integer resvCode, @RequestBody Map<String, String> updatedResvInfo){
        try{
            Date resvDate = Date.valueOf(updatedResvInfo.get("resvDate"));
            Time resvTime = Time.valueOf(updatedResvInfo.get("resvTime"));

            BossResvInputDTO inputDTO = new BossResvInputDTO();
            inputDTO.setResvDate(resvDate);
            inputDTO.setResvTime(resvTime);
            inputDTO.setMenuName(updatedResvInfo.get("menuName"));
            inputDTO.setUserComment(updatedResvInfo.get("userComment"));

            bossReservationService.updateReservation(inputDTO, resvCode, shopCode);

            return ResponseEntity.ok().body(
                    new ResponseMessage(200, "수정 성공", null));
        } catch (Exception e){

            return ResponseEntity.status(400)
                    .header("Content-Type", "application/json")
                    .body(null);
        }
    }

    @PatchMapping(value = "/{resvCode}", produces = "application/json")
    public ResponseEntity<ResponseMessage> softDeleteReservation(@PathVariable("shopCode") Integer shopCode, @PathVariable("resvCode") Integer resvCode){
        try{
            bossReservationService.cancelReservation(resvCode);

            return ResponseEntity.ok().body(
                    new ResponseMessage(200, "논리적 삭제 성공", null));
        } catch (Exception e){
            e.printStackTrace();

            return ResponseEntity.status(400)
                    .header("Content-Type", "application/json")
                    .body(null);
        }
    }
}
