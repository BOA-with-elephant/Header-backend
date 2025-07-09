package com.header.header.domain.shop.repository;

import com.header.header.domain.shop.dto.ShopSummaryDTO;
import com.header.header.domain.shop.entity.Shop;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ShopRepository extends JpaRepository<Shop, Integer> {

    @Query("SELECT s FROM Shop s WHERE s.adminCode = :adminCode AND s.isActive = true")
    List<Shop> findShopsByAdminCode(Integer adminCode);

    @Query("SELECT new com.header.header.domain.shop.dto.ShopSummaryDTO " +
            "(s.shopCode, s.categoryCode, s.adminCode, s.shopName, s.shopLocation, s.shopStatus) " +
            "FROM Shop s WHERE s.adminCode = :adminCode AND s.isActive = true")
    List<ShopSummaryDTO> findShopsSummaryByAdminCode(@Param("adminCode") Integer adminCode);
}
