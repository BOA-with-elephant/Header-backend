package com.header.header.domain.visitors.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class VisitorCreateResponse {
    private Integer clientCode;
    private Boolean sendable;
    private String memo;
}
