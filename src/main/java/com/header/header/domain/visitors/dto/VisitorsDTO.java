package com.header.header.domain.visitors.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Builder
@ToString
public class VisitorsDTO {

    private Integer clientCode;
    @NotBlank
    private Integer userCode;
    @NotBlank
    private Integer shopCode;
    private String memo;
    private boolean sendable;
    private boolean isActive;
}
