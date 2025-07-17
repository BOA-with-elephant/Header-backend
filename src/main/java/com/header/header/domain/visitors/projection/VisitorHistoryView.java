package com.header.header.domain.visitors.projection;

import java.time.LocalDate;

public interface VisitorHistoryView {
    LocalDate getVisitDate();
    String getMenuName();
}
