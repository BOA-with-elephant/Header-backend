package com.header.header.domain.visitors.service;

import com.header.header.domain.visitors.DTO.VisitorDetailDTO;
import com.header.header.domain.visitors.projection.UserFavoriteMenuView;
import com.header.header.domain.visitors.projection.VisitStatisticsView;
import com.header.header.domain.visitors.projection.VisitorWithUserInfoView;
import com.header.header.domain.visitors.repository.VisitorsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisitorsService {

    private final VisitorsRepository visitorsRepository;

    /**
     * 샵 고객 리스트 조회
     *
     * @param shopCode 어떤 샵의 고객 리스트를 가져올지
     * @return List<VisitorDetailDTO>
     */
    public List<VisitorDetailDTO> getShopVisitorsList(Integer shopCode){
        if(shopCode == null){
            throw new IllegalArgumentException("shopCode는 필수입니다.");
        }

        // 1. 기본 방문자 정보
        List<VisitorWithUserInfoView> visitors = visitorsRepository.findVisitorWithUserInfoByShopCode(shopCode);

        // 2. 모든 userCode 추출
        List<Integer> userCodes = visitors.stream()
                .map(VisitorWithUserInfoView::getUserCode)
                .collect(Collectors.toList());

        // 3. 배치로 통계 정보 조회
        Map<Integer, VisitStatisticsView> statisticsMap = getVisitStatisticsBatch(userCodes);
        Map<Integer, String> favoriteMenuMap = getFavoriteMenusBatch(userCodes);

        return visitors.stream()
                .map(visitor -> {
                    VisitStatisticsView stats = statisticsMap.get(visitor.getUserCode());
                    String favoriteMenu = favoriteMenuMap.get(visitor.getUserCode());

                    return VisitorDetailDTO.builder()
                            .clientCode(visitor.getClientCode())
                            .userCode(visitor.getUserCode())
                            .memo(visitor.getMemo())
                            .sendable(visitor.getSendable())
                            .userName(visitor.getUserName())
                            .userPhone(visitor.getUserPhone())
                            .birthday(visitor.getBirthday() != null ?
                                    visitor.getBirthday().toLocalDate() : null)
                            // 통계 정보
                            .visitCount(stats != null ? stats.getVisitCount() : 0)
                            .totalPaymentAmount(stats != null ? stats.getTotalPaymentAmount() : 0)
                            .lastVisitDate(stats != null ? stats.getLastVisitDate().toLocalDate() : null)
                            // 선호 메뉴
                            .favoriteMenuName(favoriteMenu != null ? favoriteMenu : "" )
                            .build();
                })
                .collect(Collectors.toList());
    }

    /* 방문자 조회 리스트를 Map<회원 코드, 방문 통계 정보>으로 변환 */
    private Map<Integer, VisitStatisticsView> getVisitStatisticsBatch(List<Integer> userCodes) {
        List<VisitStatisticsView> statistics = visitorsRepository.getVisitStatisticsBatch(userCodes);
        return statistics.stream()
                .collect(Collectors.toMap(VisitStatisticsView::getUserCode, Function.identity()));
    }

    /* 방문자 선호 메뉴 리스트를 Map<회원 코드 , 선호 메뉴> 으로 변환 */
    private Map<Integer, String> getFavoriteMenusBatch(List<Integer> userCodes) {
        // JPQL로 모든 메뉴 데이터를 가져와서 Java에서 처리
        List<UserFavoriteMenuView> rawMenus = visitorsRepository.getUserFavoriteMenusRaw(userCodes);

        // 각 유저별로 가장 많이 주문한 메뉴(첫 번째)만 선택
        return rawMenus.stream()
                .collect(Collectors.groupingBy(UserFavoriteMenuView::getUserCode))
                .entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            UserFavoriteMenuView first = entry.getValue().get(0); // 첫 번째가 가장 많이 주문한 메뉴
                            return first.getMenuName();
                        }
                ));
    }

}
