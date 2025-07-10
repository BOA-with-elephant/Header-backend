package com.header.header.domain.shop.repository;

import com.header.header.domain.shop.entity.Shop;
import com.header.header.domain.shop.projection.ShopSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopRepository extends JpaRepository<Shop, Integer> {

    // 요약 정보(ShopSummary)를 위해 Projection Based Interface 방식 사용
    List<ShopSummary> findByAdminCodeAndIsActiveTrue(Integer adminCode);
}
