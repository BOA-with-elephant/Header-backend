package com.header.header.domain.message.projection;

import java.sql.Date;
import java.sql.Time;

public interface MessageBatchListView {
    Integer getBatchCode();

    Date getSendDate();
    Time getSendTime();
    Integer getTotalCount();
    String getSendType();
    String getSubject();
}
