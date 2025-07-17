package com.header.header.domain.visitors.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class VisitorHistoryResponse {
    private LocalDate visitDate;
    private String menuName;
}
