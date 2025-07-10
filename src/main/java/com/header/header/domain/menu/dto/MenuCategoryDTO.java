package com.header.header.domain.menu.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MenuCategoryDTO {

    private Integer categoryCode;

    @NotNull(message = "샵 코드는 필수입니다.")
    private Integer shopCode;

    @NotBlank(message = "카테고리 이름은 필수입니다.")
    private String categoryName;

    @NotBlank(message = "메뉴 컬러는 필수입니다.")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "메뉴 컬러는 '#FFFFFF' 형식이어야 합니다.")
    private String menuColor;

    @JsonProperty(defaultValue = "true")
    private Boolean isActive;
}
