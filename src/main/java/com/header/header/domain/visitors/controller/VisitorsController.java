package com.header.header.domain.visitors.controller;

import com.header.header.common.controller.MyShopBaseController;
import com.header.header.common.dto.response.ApiResponse;
import com.header.header.domain.visitors.dto.VisitorCreateRequest;
import com.header.header.domain.visitors.dto.VisitorCreateResponse;
import com.header.header.domain.visitors.dto.VisitorDetailResponse;
import com.header.header.domain.visitors.dto.VisitorHistoryResponse;
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
     *
     * @param shopId 회원 정보를 불러올 샵의 아이디
     * @return 고객 목록
     *
     * 최종 URL: GET /api/v1/my-shops/{shopId}/customers
     */
    @GetMapping("/customers")
    public ResponseEntity<ApiResponse<List<VisitorDetailResponse>>> getShopCustomerList(
            @PathVariable Integer shopId) {

        List<VisitorDetailResponse> visitors = visitorsService.getShopVisitorsList(shopId);
        return success(visitors);
    }

    /**
     * 유저를 샵의 회원으로 등록합니다.
     *
     * @param shopId 등록할 샵의 아이디
     * @param requestBody 등록할 고객 정보
     * @return 등록된 고객 정보
     *
     * 최종 URL: POST /api/v1/my-shops/{shopId}/customers
     */
    @PostMapping("/customers")
    public ResponseEntity<ApiResponse<VisitorCreateResponse>> registerCustomer(
            @PathVariable Integer shopId,
            @RequestBody VisitorCreateRequest requestBody) {

        VisitorCreateResponse visitor = visitorsService.createVisitorsByNameAndPhone(shopId, requestBody);
        return success(visitor);
    }

    /**
     * 샵 회원에 대한 방문 히스토리를 조회합니다.
     *
     * @param shopId 샵 아이디
     * @param customerId 고객 아이디
     * @return 고객의 방문 히스토리 목록
     *
     * 최종 URL: GET /api/v1/my-shops/{shopId}/customers/{customerId}
     */
    @GetMapping("/customers/{customerId}")
    public ResponseEntity<ApiResponse<List<VisitorHistoryResponse>>> getCustomerHistory(
            @PathVariable Integer shopId,
            @PathVariable Integer customerId) {

        List<VisitorHistoryResponse> historyList = visitorsService.getShopVisitorsHistory(shopId, customerId);
        return success(historyList);
    }

    /**
     * 샵 회원에 대한 메모를 수정합니다.
     *
     * @param shopId 샵 아이디
     * @param customerId 고객 아이디
     * @param memo 수정할 메모 내용
     * @return 수정된 메모 내용
     *
     * 최종 URL: PATCH /api/v1/my-shops/{shopId}/customers/{customerId}?memo={memo}
     */
    @PatchMapping("/customers/{customerId}")
    public ResponseEntity<ApiResponse<String>> updateCustomerMemo(
            @PathVariable Integer shopId,
            @PathVariable Integer customerId,
            @RequestParam String memo) {

        String updatedMemo = visitorsService.updateShopUserMemo(shopId, customerId, memo);
        return success(updatedMemo);
    }

    /**
     * 샵 회원을 삭제합니다.
     *
     * @param shopId 샵 아이디
     * @param customerId 고객 아이디
     * @return 삭제 완료 메시지
     *
     * 최종 URL: DELETE /api/v1/my-shops/{shopId}/customers/{customerId}
     */
    @DeleteMapping("/customers/{customerId}")
    public ResponseEntity<ApiResponse<String>> deleteCustomer(
            @PathVariable Integer shopId,
            @PathVariable Integer customerId) {

        visitorsService.deleteShopUser(shopId, customerId);
        return success("고객이 삭제되었습니다.");
    }


}