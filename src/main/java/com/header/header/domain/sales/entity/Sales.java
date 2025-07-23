package com.header.header.domain.sales.entity;


import com.header.header.domain.sales.enums.PaymentStatus;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="tbl_sales")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Sales {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int salesCode;

    private Integer resvCode;
    private int payAmount;
    private String payMethod;
    private LocalDateTime payDatetime;

    @Enumerated(EnumType.STRING)  // enum을 String으로 DB 저장
    private PaymentStatus payStatus;

    private Integer cancelAmount;
    private LocalDateTime cancelDatetime;
    private String cancelReason;
    private Integer finalAmount;

    // === 비즈니스 메소드 ===

    public void updatePaymentInfo(String payMethod, String cancelReason) {
        if (payMethod != null) {
            this.payMethod = payMethod;
        }
        if (cancelReason != null) {
            this.cancelReason = cancelReason;
        }
    }

    // 수정: 파라미터 타입을 PaymentStatus로 변경
    public void updatePaymentStatus(PaymentStatus payStatus) {
        this.payStatus = payStatus;
    }

    // 수정: newStatus를 PaymentStatus로 변경
    public void processCancelation(Integer cancelAmount, String cancelReason,
        PaymentStatus newStatus, Integer finalAmount) {
        this.cancelAmount = cancelAmount;
        this.cancelDatetime = LocalDateTime.now();
        this.cancelReason = cancelReason;
        this.payStatus = newStatus;
        this.finalAmount = finalAmount;
    }

    // 수정: 상태 변경 시 PaymentStatus enum 사용
    public void processPartialCancel(Integer additionalCancelAmount, String reason) {
        this.cancelAmount = (this.cancelAmount != null ? this.cancelAmount : 0) + additionalCancelAmount;
        this.cancelDatetime = LocalDateTime.now();
        this.cancelReason = reason;
        this.finalAmount = this.payAmount - this.cancelAmount;

        if (this.cancelAmount.equals(this.payAmount)) {
            this.payStatus = PaymentStatus.CANCELLED;
        } else {
            this.payStatus = PaymentStatus.PARTIAL_CANCELLED;
        }
    }

    public void calculateFinalAmount() {
        this.finalAmount = this.payAmount - (this.cancelAmount != null ? this.cancelAmount : 0);
    }

    /**
     * 결제 금액, 결제 방법, 최종 금액을 업데이트하는 메서드
     * 세터 대신 비즈니스 로직 캡슐화
     * @param payAmount 결제 금액
     * @param payMethod 결제 방법
     * @param finalAmount 최종 금액
     */
    public void updatePaymentDetails(Integer payAmount, String payMethod, Integer finalAmount) {
        // null 체크를 통해 필요한 필드만 업데이트
        if (payAmount != null) {
            this.payAmount = payAmount;
        }
        if (payMethod != null) {
            this.payMethod = payMethod;
        }
        if (finalAmount != null) {
            this.finalAmount = finalAmount;
        }
    }

}
