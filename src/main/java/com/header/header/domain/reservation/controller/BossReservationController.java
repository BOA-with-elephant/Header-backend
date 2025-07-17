package com.header.header.domain.reservation.controller;

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
    @GetMapping("/{date}")
    public ResponseEntity<List<BossResvProjectionDTO>> findReservationListOfThisMonth(@PathVariable("shopCode") Integer shopCode, @PathVariable("date") String thisMonth){
        try{
            List<BossResvProjectionDTO> reservationList = bossReservationService.findReservationList(shopCode, thisMonth);

            return ResponseEntity.ok(reservationList);
        } catch (Exception e){
            return ResponseEntity.internalServerError().build();
        }
    }
}
