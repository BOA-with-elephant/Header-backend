package com.header.header.domain.visitors.DTO;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter /* TEST 용 */
@Builder
public class VisitorDetailDTO {
    private Integer clientCode;
    private Integer userCode;
    private String memo;
    private Boolean sendable;
    private String userName;
    private String userPhone;
    private LocalDate birthday;
    private Integer visitCount;
    private Integer totalPaymentAmount;
    private LocalDate lastVisitDate;
    private String favoriteMenuName;
}
