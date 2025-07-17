package com.header.header.domain.visitors.dto;

import com.header.header.common.util.TimeUtils;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VisitorDetailResponse {
    private Integer clientCode;
    private String userName;
    private String phone;
    private String birthday;
    private Boolean sendable;
    private Integer visitCount;
    private Integer totalPaymentAmount;
    private String lastVisited; // 응답시에는 현재 시간과 비교하여 _일 전 으로 응답한다.
    private String favoriteMenuName;

    public static VisitorDetailResponse from(VisitorDetailDTO dto){
        return VisitorDetailResponse.builder()
                .clientCode(dto.getClientCode())
                .userName(dto.getUserName())
                .phone(dto.getUserPhone())
                .birthday(TimeUtils.formatBirthday(dto.getBirthday()))
                .sendable(dto.getSendable())
                .visitCount(dto.getVisitCount())
                .totalPaymentAmount(dto.getTotalPaymentAmount())
                .lastVisited(TimeUtils.toRelativeTime(dto.getLastVisitDate()))
                .favoriteMenuName(dto.getFavoriteMenuName())
                .build();
    }
}
