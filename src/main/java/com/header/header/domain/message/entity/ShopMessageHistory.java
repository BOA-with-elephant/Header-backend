package com.header.header.domain.message.entity;

import com.header.header.domain.message.enums.MessageStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Entity
@Table(name="tbl_shop_msg_history")
@Getter
@NoArgsConstructor // ( access = AccessLevel.PROTECTED)
public class ShopMessageHistory {

    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY )
    private Integer historyCode;
    private Integer batchCode;
    private Integer userCode;

    @Lob
    private String msgContent;

    @Enumerated(EnumType.STRING)
    private MessageStatus sendStatus;

    private String errorMessage;
    /* 예약 메세지일 경우에는 sentStatus가 reservation이었다가 sent로 바뀌게 된다 따라서 history의 Timestamp는 어노테이션으로
        관리하지 않는다. */
    private Timestamp sentAt;

    public void updateStatus(MessageStatus newStatus, String errorMessage){
        validateStatusTransition(newStatus);

        this.sendStatus = newStatus;

        if (newStatus == MessageStatus.SUCCESS) {
            this.sentAt = new Timestamp(System.currentTimeMillis());
            this.errorMessage = null; // 에러 메시지 초기화
        }
        else if (newStatus == MessageStatus.FAIL) {
            this.errorMessage = errorMessage;
        }

    }

    // 상태 전환 검증
    private void validateStatusTransition(MessageStatus newStatus){
        if(!this.sendStatus.canTransitionTo(newStatus)){
            throw new IllegalStateException(
                    String.format("상태전환불가: %s -> %s", this.sendStatus, newStatus)
            );
        }
    }


    // 편의 메서드들
    public void markAsSuccess() {
        updateStatus(MessageStatus.SUCCESS, null);
    }

    public void markAsFailed(String errorMessage) {
        updateStatus(MessageStatus.FAIL, errorMessage);
    }

    // test setter
    public void setMsgContent(String msgContent) {
        this.msgContent = msgContent;
    }

    public void setUserCode(Integer userCode) {
        this.userCode = userCode;
    }

    public void setSendStatus(MessageStatus sendStatus) {
        this.sendStatus = sendStatus;
    }

    public void setSentAt(Timestamp sentAt) {
        this.sentAt = sentAt;
    }
}
