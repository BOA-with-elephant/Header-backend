package com.header.header.domain.message.repository;

import com.header.header.domain.message.entity.ShopMessageHistory;
import com.header.header.domain.message.projection.MessageContentView;
import com.header.header.domain.message.projection.MessageHistoryListView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShopMessageHistoryRepository extends JpaRepository<ShopMessageHistory, Integer> {

    @Query("SELECT h.historyCode as historyCode, " +
            "       h.batchCode as batchCode, " +
            "       h.userCode as userCode, " +
            "       u.userName as userName, " +
            "       h.sentStatus as sentStatus, " +
            "       h.sentAt as sentAt " +
            "FROM ShopMessageHistory h " +
            "INNER JOIN User u ON h.userCode = u.userCode " +
            "WHERE h.batchCode = :batchCode")
    List<MessageHistoryListView> findByBatchCode(@Param("batchCode") Integer batchCode);

    Optional<MessageContentView> findByBatchCodeAndUserCode(Integer batchCode, Integer userCode);

    Optional<ShopMessageHistory> findByHistoryCode(Integer historyCode);
}
