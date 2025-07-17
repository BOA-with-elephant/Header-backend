package com.header.header.domain.sales.controller;

import com.header.header.domain.sales.dto.SalesDetailDTO;
import com.header.header.domain.sales.service.SalesService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"},
    allowedHeaders = "*",
    methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class SalesController {

    private static final Logger log = LoggerFactory.getLogger(SalesController.class);

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    /**
     * 특정 샵의 전체 매출 조회 (삭제 제외)
     * @param shopCode 조회할 샵의 코드
     * @return 해당 샵의 모든 SalesDetail DTO 리스트
     */
    @GetMapping("/myshop/{shopCode}/sales/active")
    public ResponseEntity<List<SalesDetailDTO>> getAllSales(@PathVariable Integer shopCode) {
        try {
            log.debug("샵코드 {}의 메뉴 매출 조회 요청", shopCode);

            List<SalesDetailDTO> sales = salesService.getActiveSalesDetailsByShop(shopCode);

            log.debug("샵코드 {}의 매출 조회 완료. 조회된 개수: {}", shopCode, sales.size());

            return ResponseEntity.ok(sales);

        } catch (Exception e) {
            log.error("샵코드 {}의 메출 조회 중 오류 발생", shopCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 매출 삭제 (논리적 삭제)
     * @param shopCode 삭제할 매출이 속한 샵 코드
     * @param salesCode 삭제할 매출 코드
     * @return 삭제 완료 응답
     */
    @DeleteMapping("/myshop/{shopCode}/sales/{salesCode}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Integer shopCode, @PathVariable Integer salesCode) {
        try {
            log.debug("샵코드 {}, 매출코드 {}의 매출 삭제 요청", shopCode, salesCode);

            salesService.deleteSales(salesCode);

            log.info("샵코드 {}, 매출코드 {}의 매출 삭제 완료", shopCode, salesCode);

            return ResponseEntity.noContent().build();

        } catch (Exception e) {
            log.error("샵코드 {}, 매출코드 {}의 매출 삭제 중 오류 발생", shopCode, salesCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }


}
