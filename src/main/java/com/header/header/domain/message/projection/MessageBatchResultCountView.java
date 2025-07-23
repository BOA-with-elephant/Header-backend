package com.header.header.domain.message.projection;

public interface MessageBatchResultCountView {
    Integer getSuccessCount();
    Integer getFailCount();
}
