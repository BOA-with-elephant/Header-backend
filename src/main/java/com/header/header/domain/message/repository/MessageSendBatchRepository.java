package com.header.header.domain.message.repository;

import com.header.header.domain.message.entity.MessageSendBatch;
import com.header.header.domain.message.projection.MessageBatchListView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MessageSendBatchRepository extends JpaRepository<MessageSendBatch, Integer> {

    /* 샵코드로 전체 메세지 배치 리스트 조회하기( 배치 코드 내림차순으로 ) */
    List<MessageBatchListView> findByShopCodeOrderByBatchCodeDesc(Integer shopCode);

    /* 샵의 배치 리스트 중에서 배치 코드의 전체 엔티티를 조회한다. */
    Optional<MessageSendBatch> findByShopCodeAndBatchCode(Integer shopCode, Integer batchCode);
}
