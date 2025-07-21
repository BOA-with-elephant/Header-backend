package com.header.header.domain.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
/* Service에서 사용하는 메세지 DTO 입니다. */
@Data
@Builder
public class MessageDTO {
    @NotBlank(message = "유저 코드는 필수입니다")
    private Integer to;

    @NotBlank(message = "발신 샵 코드는 필수입니다")
    private Integer from;

    @NotBlank(message = "전송 타입은 필수입니다.")
    private String sendType;

    @NotBlank(message = "제목은 필수 입니다.")
    private String subject;

    @NotBlank(message = "메시지 내용은 필수입니다")
    @Size(max = 1000, message = "메시지는 1000자 이하여야 합니다")
    private String text;
}
