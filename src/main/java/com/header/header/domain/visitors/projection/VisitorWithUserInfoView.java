package com.header.header.domain.visitors.projection;

import java.time.LocalDateTime;

public interface VisitorWithUserInfoView {

    Integer getClientCode(); // PK
    Integer getUserCode();
    String getMemo(); // 샵 회원 개별 메모
    Boolean getSendable(); // 수신 거부 여부

    // Join User
    String getUserName();
    String getUserPhone();
    LocalDateTime getBirthday();
}
