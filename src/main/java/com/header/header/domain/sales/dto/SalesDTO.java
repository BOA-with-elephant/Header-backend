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

    @NotNull(message = "예약 코드는 필수입니다.")
    private Integer resvCode;

    @NotNull(message = "결제 금액은 필수입니다. (취소를 위해 음수 허용)")
    private Integer payAmount;

    @NotBlank(message = "결제 방법은 필수입니다.")
    private String payMethod;

    @NotNull(message = "결제 일시는 필수입니다.")
    private LocalDateTime payDatetime;

    @NotNull(message = "결제 상태는 필수입니다.")
    private PaymentStatus payStatus = PaymentStatus.COMPLETED;

    private Integer cancelAmount = 0;
    private LocalDateTime cancelDatetime;
    private String cancelReason;

    private String statusNote;

    @NotNull(message = "최종 결제 금액은 필수입니다.")
    private Integer finalAmount;

    // === 편의 메소드 ===

    /**
     * 현재 결제 상태에서 취소가 가능한지 확인
     * @return COMPLETED 또는 PARTIAL_CANCELLED 상태일 때 true
     */
    public boolean canCancel() {
        return payStatus != null && payStatus.isCancellable();
    }

    /**
     * 현재 결제가 취소된 상태인지 확인
     * @return CANCELLED 또는 PARTIAL_CANCELLED 상태일 때 true
     */
    public boolean isCancelled() {
        return payStatus != null && payStatus.isCancelled();
    }

    /**
     * 결제 상태를 변경하고 상태 메모를 업데이트
     * @param newStatus 새로운 결제 상태
     * @param note 상태 변경에 대한 메모
     */
    public void updateStatus(PaymentStatus newStatus, String note) {
        this.payStatus = newStatus;
        this.statusNote = note;
    }

    /**
     * 결제 취소를 처리하는 메인 메소드
     * - 취소 가능 여부 검증
     * - 취소 금액에 따라 전체/부분 취소 상태 결정
     * - 최종 결제 금액 계산
     *
     * @param cancelAmount 취소할 금액
     * @param reason 취소 사유
     * @param note 취소에 대한 추가 메모
     * @throws IllegalStateException 취소 불가능한 상태일 때
     */
    public void processCancel(Integer cancelAmount, String reason, String note) {
        if (!canCancel()) {
            throw new IllegalStateException("현재 상태에서는 취소할 수 없습니다: " + payStatus.name());
        }

        this.cancelAmount = cancelAmount;
        this.cancelDatetime = LocalDateTime.now();
        this.cancelReason = reason;
        this.statusNote = note;

        // 취소 금액이 전체 결제 금액과 같으면 전체 취소, 아니면 부분 취소
        if (cancelAmount.equals(this.payAmount)) {
            this.payStatus = PaymentStatus.CANCELLED;
        } else {
            this.payStatus = PaymentStatus.PARTIAL_CANCELLED;
        }

        this.finalAmount = this.payAmount - cancelAmount;
    }

    /**
     * 간단한 결제 취소 처리 (사유와 메모 없이)
     * @param cancelAmount 취소할 금액
     */
    public void processCancel(Integer cancelAmount) {
        processCancel(cancelAmount, null, null);
    }

    /**
     * 결제 상태만 변경 (메모 없이)
     * @param newStatus 새로운 결제 상태
     */
    public void changeStatusOnly(PaymentStatus newStatus) {
        this.payStatus = newStatus;
    }

    /**
     * 결제 상태 변경과 함께 메모 추가
     * @param newStatus 새로운 결제 상태
     * @param note 상태 변경에 대한 메모
     */
    public void changeStatusWithNote(PaymentStatus newStatus, String note) {
        this.payStatus = newStatus;
        this.statusNote = note;
    }
}