package com.header.header.domain.visitors.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class VisitorsDTO {

    private Integer clientCode;
    @NotBlank
    private Integer userCode;
    @NotBlank
    private Integer shopCode;
    private String memo;
    @NotBlank
    private boolean sendable;
    @NotBlank
    private boolean isActive;
}
