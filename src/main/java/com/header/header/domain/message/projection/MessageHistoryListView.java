package com.header.header.domain.message.projection;

import java.sql.Timestamp;

public interface MessageHistoryListView {

    Integer getHistoryCode();
    Integer getBatchCode();
    Integer getUserCode();
    String getUserName();
    String getSentStatus();
    Timestamp getSentAt();
    String getErrorMessage();
}
