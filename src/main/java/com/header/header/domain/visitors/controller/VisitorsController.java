package com.header.header.domain.visitors.controller;

import com.header.header.common.controller.MyShopBaseController;

import com.header.header.common.dto.response.ApiResponse;
import com.header.header.domain.visitors.dto.VisitorDetailResponse;
import com.header.header.domain.visitors.service.VisitorsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class VisitorsController extends MyShopBaseController {

    private final VisitorsService visitorsService;

    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<List<VisitorDetailResponse>>> getShopCustomerList(@PathVariable Integer shopId){
        List<VisitorDetailResponse> visitors = visitorsService.getShopVisitorsList(shopId);

        return ResponseEntity.ok(ApiResponse.success(visitors));
    }

}
