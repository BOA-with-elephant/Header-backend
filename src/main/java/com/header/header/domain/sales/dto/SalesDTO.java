package com.header.header.domain.sales.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.sql.Time;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SalesDTO {

    private Integer salesCode;

    @NotBlank(message = "메뉴 이름은 필수입니다.")
    private Integer resvCode;
    @NotBlank(message = "메뉴 이름은 필수입니다.")
    private Integer payAmount;
    @NotBlank(message = "메뉴 이름은 필수입니다.")
    private String payMethod;
    @NotBlank(message = "메뉴 이름은 필수입니다.")
    private Time payDatetime;
    // 완료, 취소, 삭제 등으로 관리하고 싶어. 기본은 완료
    private String payStatus;

}
