package com.header.header.domain.shop.dto;

import com.header.header.domain.shop.enums.ShopStatus;
import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
public class ShopSummaryDTO {

    /* 관리자 코드로 전체 조회시 필요한 정보만 담을 수 있도록 한 요약 정보용 DTO
       필요하지 않은 값의 필드를 불러내는 것을 방지해 성능 향상 */

    private Integer shopCode;
    private Integer categoryCode;
    private Integer adminCode;
    private String shopName;
    private String shopLocation;
    private ShopStatus shopStatus;
}
