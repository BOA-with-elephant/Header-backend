package com.header.header.domain.visitors.controller;

import com.header.header.common.controller.MyShopBaseController;

import com.header.header.common.dto.response.ApiResponse;
import com.header.header.domain.visitors.dto.VisitorCreateRequest;
import com.header.header.domain.visitors.dto.VisitorCreateResponse;
import com.header.header.domain.visitors.dto.VisitorDetailResponse;
import com.header.header.domain.visitors.service.VisitorsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class VisitorsController extends MyShopBaseController {

    private final VisitorsService visitorsService;

    /**
     * 샵에 대한 회원 리스트를 조회합니다.
     * @param shopId 회원 정보를 불러올 샵의 아이디
     * @return ResponseEntity<ApiResponse<List<VisitorDetailResponse>>>
     */
    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<List<VisitorDetailResponse>>> getShopCustomerList(@PathVariable Integer shopId)
    {
        List<VisitorDetailResponse> visitors = visitorsService.getShopVisitorsList(shopId);

        return ResponseEntity.ok(ApiResponse.success(visitors));
    }

    /**
     * 유저를 샵의 회원으로 등록합니다.
     * @param shopId 등록할 샵의 아이디
     * @return ResponseEntity<ApiResponse<VisitorCreateResponse>>
     */
    @PostMapping("/customers")
    public ResponseEntity<ApiResponse<VisitorCreateResponse>> registerCustomer(
            @PathVariable Integer shopId,
            @RequestBody VisitorCreateRequest requestBody)
    {

        VisitorCreateResponse visitor = visitorsService.createVisitorsByNameAndPhone(shopId ,requestBody);

        return ResponseEntity.ok(ApiResponse.success(visitor));
    }

    @PatchMapping("/customers/{customerId}")
    public ResponseEntity<ApiResponse<String>> modifyCustomerMemo(
            @PathVariable Integer shopId,
            @PathVariable Integer customerId,
            @RequestParam String memo) // QueryParameter 이용!
    {
        String updatedMemo = visitorsService.updateShopUserMemo(shopId, customerId, memo);

        return ResponseEntity.ok(ApiResponse.success(updatedMemo));
    }

}
