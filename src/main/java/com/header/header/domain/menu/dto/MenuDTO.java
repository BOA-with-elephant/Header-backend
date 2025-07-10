package com.header.header.domain.menu.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MenuDTO {

    private Integer menuCode; // 생성 시에는 null, 조회 시에만 존재

    @NotBlank(message = "메뉴 이름은 필수입니다.")
    private String menuName;

    @NotNull(message = "메뉴 가격은 필수입니다.")
    @Positive(message = "메뉴 가격은 0보다 커야 합니다.")
    private Integer menuPrice;

    @PositiveOrZero(message = "예상 소요 시간은 0 이상이어야 합니다.")
    private Integer estTime;

    @JsonProperty(defaultValue = "true")
    private Boolean isActive;

    @NotNull(message = "카테고리 코드는 필수입니다.")
    private Integer categoryCode;

    @NotNull(message = "샵 코드는 필수입니다.")
    private Integer shopCode;

    private String categoryName; // 조회 시에만 표시
    private String menuColor;    // 조회 시에만 표시

}
