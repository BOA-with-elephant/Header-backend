package com.header.header.domain.shop.repository;

import com.header.header.domain.shop.dto.ShopSummaryDTO;
import com.header.header.domain.shop.entity.Shop;
import com.header.header.domain.shop.projection.ShopSummary;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ShopRepository extends JpaRepository<Shop, Integer> {

    // 요약 정보를 위한 DTO(ShopSummaryDTO)를 사용하기 위해 @Query로 처리
    @Query("SELECT new com.header.header.domain.shop.dto.ShopSummaryDTO " +
            "(s.shopCode, s.categoryCode, s.adminCode, s.shopName, s.shopLocation, s.shopStatus) " +
            "FROM Shop s WHERE s.adminCode = :adminCode AND s.isActive = true")
    List<ShopSummaryDTO> findShopsSummaryByAdminCode(@Param("adminCode") Integer adminCode);

    List<ShopSummary> findByAdminCodeAndShopStatusTrue(Integer adminCode);

    //상단 쿼리는 추후 일정 조정 후 Interface로 방식으로 바꿀 수 있음
}
