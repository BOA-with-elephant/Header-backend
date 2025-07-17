package com.header.header.domain.visitors.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class VisitorCreateRequest{
    private String name;
    
    @JsonFormat(pattern = "yyyy-MM-dd") // 클라이언트에서 보내주는 형식
    private LocalDate birthday;
    
    private String phone;
    private Boolean sendable;
    private String memo;
}
