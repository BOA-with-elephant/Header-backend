package com.header.header.domain.message.projection;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Time;

public interface MessageBatchListView {
    Integer getBatchCode();

    Date getSendDate();
    Time getSendTime();

    String getSendType();
    String getSubject();



}
