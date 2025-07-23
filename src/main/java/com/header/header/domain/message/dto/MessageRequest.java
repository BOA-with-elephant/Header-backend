package com.header.header.domain.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageRequest {

    @NotBlank(message = "메시지 내용은 필수입니다.")
    @Size(max = 1000, message = "메시지는 1000자 이내로 작성해주세요.")
    private String messageContent;

    @NotEmpty(message = "수신자를 선택해주세요.")
    @Size(max = 100, message = "한 번에 최대 100명까지 발송 가능합니다.")
    private List<Integer> clientCodes; // Front에서 요청을 받을 때는 Client 코드로 요청을 받는다.

    @NotNull(message = "발신 샵 코드는 필수입니다")
    private Integer shopCode;

    @NotBlank(message = "제목은 필수 입니다.")
    private String subject;        // 메시지 제목

    private Boolean isScheduled;        // 예약 발송 여부
    private LocalDateTime scheduledDateTime; // 예약 발송 시간

}
