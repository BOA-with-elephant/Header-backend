package com.header.header.domain.sales.dto;

import com.header.header.domain.sales.enums.PaymentStatus;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class SalesDTO {

    private Integer salesCode;

    // resvCode를 선택사항으로 변경 (예약 연결 없는 직접 등록 허용)
    private Integer resvCode;

    @NotNull(message = "결제 금액은 필수입니다.")
    @Min(value = 1, message = "결제 금액은 1원 이상이어야 합니다.")
    private Integer payAmount;

    @NotBlank(message = "결제 방법은 필수입니다.")
    private String payMethod;

    private LocalDateTime payDatetime; // 기본값은 컨트롤러에서 설정

    private PaymentStatus payStatus = PaymentStatus.COMPLETED;

    private Integer cancelAmount = 0;
    private LocalDateTime cancelDatetime;
    private String cancelReason;
    private String statusNote;
    private Integer finalAmount; // 기본값은 컨트롤러에서 payAmount로 설정

    // 프론트엔드에서 전송하는 추가 정보 (매출 생성 시 참조용)
    private String userName;
    private String userPhone;
    private String menuName;
    private Integer menuPrice;

    // === 편의 메소드들은 그대로 유지 ===

    public boolean canCancel() {
        return payStatus != null && payStatus.isCancellable();
    }

    public boolean isCancelled() {
        return payStatus != null && payStatus.isCancelled();
    }

    public void updateStatus(PaymentStatus newStatus, String note) {
        this.payStatus = newStatus;
        this.statusNote = note;
    }

    public void processCancel(Integer cancelAmount, String reason, String note) {
        if (!canCancel()) {
            throw new IllegalStateException("현재 상태에서는 취소할 수 없습니다: " + payStatus.name());
        }

        this.cancelAmount = cancelAmount;
        this.cancelDatetime = LocalDateTime.now();
        this.cancelReason = reason;
        this.statusNote = note;

        if (cancelAmount.equals(this.payAmount)) {
            this.payStatus = PaymentStatus.CANCELLED;
        } else {
            this.payStatus = PaymentStatus.PARTIAL_CANCELLED;
        }

        this.finalAmount = this.payAmount - cancelAmount;
    }

    public void processCancel(Integer cancelAmount) {
        processCancel(cancelAmount, null, null);
    }

    public void changeStatusOnly(PaymentStatus newStatus) {
        this.payStatus = newStatus;
    }

    public void changeStatusWithNote(PaymentStatus newStatus, String note) {
        this.payStatus = newStatus;
        this.statusNote = note;
    }
}