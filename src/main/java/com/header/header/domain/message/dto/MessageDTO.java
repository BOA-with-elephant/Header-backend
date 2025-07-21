package com.header.header.domain.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

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

    private Integer templateCode; // 필수 X

    @NotBlank(message = "메시지 내용은 필수입니다")
    @Size(max = 2000, message = "메시지는 2000자 이하여야 합니다")
    private String text;
}
