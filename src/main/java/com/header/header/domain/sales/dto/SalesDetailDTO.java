package com.header.header.domain.sales.dto;

import com.header.header.domain.sales.enums.PaymentStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.sql.Date;
import java.sql.Time;

/**
 * 매출 상세 조회용 DTO
 * - Sales, Reservation, User, Menu 정보를 통합
 */
@Getter
@Setter
@ToString
public class SalesDetailDTO {

    // === Sales 관련 ===
    private Integer salesCode;
    private Integer resvCode;
    private Integer payAmount;
    private String payMethod;
    private LocalDateTime payDatetime;
    private PaymentStatus payStatus;
    private Integer cancelAmount;
    private LocalDateTime cancelDatetime;
    private String cancelReason;
    private Integer finalAmount;

    // === Reservation 관련 ===
    private Integer shopCode;
    private Integer userCode;
    private Integer menuCode;
    private Date resvDate;
    private Time resvTime;

    // === User / Menu 관련 ===
    private String userName;
    private String menuName;
    private Integer menuPrice;

    /**
     * JPQL 결과 매핑용 생성자
     * - 파라미터 순서는 JPQL SELECT new 구문과 일치해야 함
     */
    public SalesDetailDTO(
        Integer salesCode,
        Integer resvCode,
        Integer payAmount,
        String payMethod,
        LocalDateTime payDatetime,
        PaymentStatus payStatus,
        Integer cancelAmount,
        LocalDateTime cancelDatetime,
        String cancelReason,
        Integer finalAmount,
        Integer shopCode,
        Integer userCode,
        Integer menuCode,
        Date resvDate,
        Time resvTime,
        String userName,
        String menuName,
        Integer menuPrice
    ) {
        this.salesCode = salesCode;
        this.resvCode = resvCode;
        this.payAmount = payAmount;
        this.payMethod = payMethod;
        this.payDatetime = payDatetime;
        this.payStatus = payStatus;
        this.cancelAmount = cancelAmount;
        this.cancelDatetime = cancelDatetime;
        this.cancelReason = cancelReason;
        this.finalAmount = finalAmount;
        this.shopCode = shopCode;
        this.userCode = userCode;
        this.menuCode = menuCode;
        this.resvDate = resvDate;
        this.resvTime = resvTime;
        this.userName = userName;
        this.menuName = menuName;
        this.menuPrice = menuPrice;
    }

}
