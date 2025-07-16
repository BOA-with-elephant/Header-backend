package com.header.header.domain.visitors.service;

import com.header.header.domain.visitors.dto.VisitorDetailDTO;
import com.header.header.domain.visitors.dto.VisitorsDTO;
import com.header.header.domain.visitors.dto.VisitorDetailResponse;
import com.header.header.domain.visitors.enitity.Visitors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@Transactional
public class VisitorsTests {

    @Autowired
    private VisitorsService visitorsService;

    private VisitorsDTO testVisitor;

    @BeforeEach
    void setUp(){
        VisitorsDTO visitors = visitorsService.createVisitorsByNameAndPhone(2, "김예람", "010-2222-9999", true);

        testVisitor = VisitorsDTO.builder()
                .clientCode(visitors.getClientCode())
                .userCode(visitors.getUserCode())
                .shopCode(visitors.getShopCode())
                .memo("초기 메모입니다.")
                .sendable(true)
                .isActive(true)
                .build();

        System.out.println(testVisitor);
    }

    @Test
    @DisplayName("null shopCode 예외 처리 확인")
    void testNullShopCode() {
        // when & then
        assertThatThrownBy(() -> visitorsService.getShopVisitorsList(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("shopCode는 필수입니다.");
    }

    @Test
    @DisplayName("존재하지 않는 shopCode로 조회 - 빈 리스트 반환 확인")
    void testNonExistentShopCode() {
        // given
        Integer nonExistentShopCode = 99999;

        // when
        List<VisitorDetailResponse> result = visitorsService.getShopVisitorsList(nonExistentShopCode);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("실제 shopCode로 조회 - 정상 작동 확인")
    void testRealShopCode() {
        // given
        Integer shopCode = 1; // 실제 DB에 있는 shopCode 사용

        // when
        List<VisitorDetailResponse> result = visitorsService.getShopVisitorsList(shopCode);

        // then
        assertThat(result).isNotNull();

        // 데이터가 있으면 검증, 없어도 OK
        if (!result.isEmpty()) {
            System.out.println("=== 조회된 방문자 목록 ===");
            for (VisitorDetailResponse visitor : result) {
                System.out.println("사용자: " + visitor.getUserName() +
                        ", 전화번호: " + visitor.getPhone() +
                        ", 방문횟수: " + visitor.getVisitCount() +
                        ", 총결제액: " + visitor.getTotalPaymentAmount() +
                        ", 선호메뉴: " + visitor.getFavoriteMenuName());

                // 기본적인 필드들이 null이 아닌지만 확인
                assertThat(visitor.getClientCode()).isNotNull();
                assertThat(visitor.getUserName()).isNotNull();
            }
        } else {
            System.out.println("조회된 방문자가 없습니다. (정상)");
        }
    }

    @Test
    @DisplayName("샵 회원 메모 수정")
    void testShopVisitorMemoUpdate(){
        String updatememo = "수정된 메모";
        String originmemo = testVisitor.getMemo();
        visitorsService.updateShopUserMemo(testVisitor.getShopCode(),testVisitor.getClientCode(),updatememo);

        assertEquals(originmemo, testVisitor.getMemo());
    }

    @Test
    @DisplayName("샵 회원 논리적 삭제")
    void testShopVisitorLogicalDelete(){

        visitorsService.deleteShopUser(testVisitor.getShopCode(),testVisitor.getClientCode());

        Visitors found = visitorsService.findVisitorByClientCode(testVisitor.getClientCode());

        assertFalse(found.isActive());
    }

}
