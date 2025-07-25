package com.header.header.domain.visitors.projection;

import java.time.LocalDate;

public interface VisitStatisticsView {
    Integer getUserCode();
    Integer getVisitCount(); // reservation에서 결제 완료 건을 COUNT
    Integer getTotalPaymentAmount(); // reservation에서 결제 완료 건의 결제금액을 SUM
    LocalDate getLastVisitDate(); // 마지막 방문일
}
