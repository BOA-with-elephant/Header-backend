package com.header.header.domain.visitors.service;

import com.header.header.domain.message.exception.InvalidBatchException;
import com.header.header.domain.user.service.UserService;
import com.header.header.domain.visitors.dto.*;
import com.header.header.domain.visitors.enitity.Visitors;
import com.header.header.domain.visitors.projection.UserFavoriteMenuView;
import com.header.header.domain.visitors.projection.VisitStatisticsView;
import com.header.header.domain.visitors.projection.VisitorWithUserInfoView;
import com.header.header.domain.visitors.repository.VisitorsRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VisitorsService {

    private final UserService userService;
    private final VisitorsRepository visitorsRepository;
    private final ModelMapper modelMapper;
    private final RestClient.Builder builder;


    /**
     * 샵 고객 리스트 조회
     *
     * @param shopCode 어떤 샵의 고객 리스트를 가져올지
     * @return List<VisitorDetailResponse>
     */
    public List<VisitorDetailResponse> getShopVisitorsList(Integer shopCode){
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

                    return  VisitorDetailResponse.from(
                            VisitorDetailDTO.builder()
                            .clientCode(visitor.getClientCode())
                            .userCode(visitor.getUserCode())
                            .memo(visitor.getMemo())
                            .sendable(visitor.getSendable())
                            .userName(visitor.getUserName())
                            .userPhone(visitor.getUserPhone())
                            .birthday(visitor.getBirthday() != null ?
                                    visitor.getBirthday() : null)
                            // 통계 정보
                            .visitCount(stats != null ? stats.getVisitCount() : 0)
                            .totalPaymentAmount(stats != null ? stats.getTotalPaymentAmount() : 0)
                            .lastVisitDate(stats != null ? stats.getLastVisitDate() : null)
                            // 선호 메뉴
                            .favoriteMenuName(favoriteMenu != null ? favoriteMenu : "" )
                            .build());
                })
                .collect(Collectors.toList());
    }

    /**
     * 샵 고객 리스트 추가
     * 사장님이 직접 고객을 추가하거나, 샵을 처음 예약한 고객일 경우 비즈니스 로직에 의해 추가된다.
     * @param shopCode 샵 코드
     * @param request 요청받은 유저 요청 DTO
     * */
    @Transactional
    public VisitorCreateResponse createVisitorsByNameAndPhone(Integer shopCode , VisitorCreateRequest request){
        // 이미 user로 존재하는지 체크
        Integer userCode = userService.findUserByNameAndPhone(request.getName(), request.getPhone());

        // 없다면 추가해서 userId를 받아온다.
        if(userCode == null){
            userCode = userService.createUserByNameAndPhone(request.getName(), request.getPhone()).getUserCode();
        }

        VisitorsDTO visitorsDTO = VisitorsDTO.builder()
                .userCode(userCode)
                .shopCode(shopCode)
                .sendable(request.getSendable())
                .memo(request.getMemo())
                .isActive(true)
                .build();

        Visitors visitors = visitorsRepository.save(modelMapper.map(visitorsDTO, Visitors.class));

        // Visitors Table에 생성.
        return VisitorCreateResponse.builder()
                .clientCode(visitors.getClientCode())
                .memo(visitors.getMemo())
                .sendable(visitors.isSendable())
                .build();
    }

    /**
     * 샵 고객에 대한 메모를 수정
     *
     * @param shopCode 샵 코드
     * @param clientCode 샵 방문자 코드( PK이기 때문에 clientCode로 검색해야 INDEXING 가능 )
     * @param memo 수정할 메모
     * */
    @Transactional              // todo. shopCode, clientCode INDEXING을 고려한 매개변수 선정
    public String updateShopUserMemo(Integer shopCode, Integer clientCode, String memo){
        Visitors found = findVisitorByClientCode(clientCode);

        found.modifyClientMemo(memo); // Entity Update

        return found.getMemo();
    }

    /**
     * 샵 고객 논리적 삭제
     *
     * @param shopCode 샵 코드
     * @param clientCode 샵 방문자 코드( PK이기 때문에 clientCode로 검색해야 INDEXING 가능 )
     * */
    @Transactional
    public void deleteShopUser(Integer shopCode, Integer clientCode){
        Visitors found = findVisitorByClientCode(clientCode);

        found.deleteLogical(); // 논리적 삭제 isActive = false
    }



    // == helper method ==
    // Test를 위해 public으로 변경
    public Visitors findVisitorByClientCode(Integer clientCode){
        return visitorsRepository.findByClientCode(clientCode)
                .orElseThrow(() -> InvalidBatchException.invalidBatchCode("존재하지 않는 클라이언트 코드 입니다."));
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
