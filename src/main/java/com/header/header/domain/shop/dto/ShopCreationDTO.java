package com.header.header.domain.shop.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
/*샵 생성시 정보를 받는 용도의 DTO*/
public class ShopCreationDTO {

    @NotBlank(message = "카테고리 유형은 비워둘 수 없습니다")
    private Integer categoryCode;
    private Integer adminCode;

    @NotBlank(message = "샵 이름은 비워둘 수 없습니다")
    @Size(max = 50, message = "샵 이름은 최대 50자까지 입력 가능합니다")
    @Pattern(regexp = "^[가-힣a-zA-Z0-9\\s]*$", message = "상점 이름은 한글, 영문, 숫자, 공백만 포함할 수 있습니다")
    private String shopName;

    @NotBlank(message = "전화번호는 비워둘 수 없습니다")
    @Size(max = 20, message = "전화번호는 최대 20자까지 입력 가능합니다")
    private String shopPhone;

    @NotBlank(message = "샵 주소는 비워둘 수 없습니다")
    @Size(max = 255, message = "샵 주소는 최대 255자까지 입력 가능합니다")
    private String shopLocation;
    private Double shopLong;
    private Double shopLa;

    @NotBlank(message = "샵 상태는 비워둘 수 없습니다")
    private String shopStatus;

    @NotBlank(message = "영업 시작 시간은 비워둘 수 없습니다")
    private String shopOpen;

    @NotBlank(message = "영업 종료 시간은 비워둘 수 없습니다")
    private String shopClose;
}
