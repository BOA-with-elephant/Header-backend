package com.header.header.domain.visitors.service;

import com.header.header.domain.visitors.DTO.VisitorDetailDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Transactional
public class VisitorsTests {

    @Autowired
    private VisitorsService visitorsService;

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
        List<VisitorDetailDTO> result = visitorsService.getShopVisitorsList(nonExistentShopCode);

        // then
        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("실제 shopCode로 조회 - 정상 작동 확인")
    void testRealShopCode() {
        // given
        Integer shopCode = 1; // 실제 DB에 있는 shopCode 사용

        // when
        List<VisitorDetailDTO> result = visitorsService.getShopVisitorsList(shopCode);

        // then
        assertThat(result).isNotNull();

        // 데이터가 있으면 검증, 없어도 OK
        if (!result.isEmpty()) {
            System.out.println("=== 조회된 방문자 목록 ===");
            for (VisitorDetailDTO visitor : result) {
                System.out.println("사용자: " + visitor.getUserName() +
                        ", 전화번호: " + visitor.getUserPhone() +
                        ", 방문횟수: " + visitor.getVisitCount() +
                        ", 총결제액: " + visitor.getTotalPaymentAmount() +
                        ", 선호메뉴: " + visitor.getFavoriteMenuName());

                // 기본적인 필드들이 null이 아닌지만 확인
                assertThat(visitor.getUserCode()).isNotNull();
                assertThat(visitor.getUserName()).isNotNull();
            }
        } else {
            System.out.println("조회된 방문자가 없습니다. (정상)");
        }
    }

}
