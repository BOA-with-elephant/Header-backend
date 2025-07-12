package com.header.header.domain.shop.repository;

import com.header.header.domain.shop.entity.ShopCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopCategoryRepository extends JpaRepository<ShopCategory, Integer> {
    List<ShopCategory> findAllByOrderByCategoryCodeAsc();
}
