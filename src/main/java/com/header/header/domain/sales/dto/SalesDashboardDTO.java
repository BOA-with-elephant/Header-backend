package com.header.header.domain.sales.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SalesDashboardDTO {

    private Long totalSales;
    private Long totalCancelAmount;
    private List<Object[]> paymentMethodStats;
    private List<Object[]> monthlyStats;
    private int activeSalesCount;
    private int completedSalesCount;
    private int cancelledSalesCount;
}