package com.header.header.domain.menu.repository;

import com.header.header.domain.menu.entity.MenuCategory;
import com.header.header.domain.menu.entity.MenuCategoryId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, MenuCategoryId> {

    // 특정 샵의 카테고리 조회
    @Query("SELECT mc FROM MenuCategory mc WHERE mc.id.shopCode = :shopCode ORDER BY mc.id.categoryCode ASC")
    List<MenuCategory> findAllByShopCodeOrderByCategoryCodeAsc(@Param("shopCode") int shopCode);

    // 특정 샵의 활성화된 카테고리 조회
    @Query("SELECT mc FROM MenuCategory mc WHERE mc.id.shopCode = :shopCode AND mc.isActive = true ORDER BY mc.id.categoryCode ASC")
    List<MenuCategory> findActiveMenuCategoriesByShopCode(@Param("shopCode") int shopCode);

    // 특정 샵의 다음 카테고리 코드 조회 (자동 증가 위함)
    @Query("SELECT COALESCE(MAX(mc.id.categoryCode), 0) + 1 FROM MenuCategory mc WHERE mc.id.shopCode = :shopCode")
    int findNextCategoryCodeByShopCode(@Param("shopCode") int shopCode);

    // 특정 샵의 카테고리명으로 검색
    @Query("SELECT mc FROM MenuCategory mc WHERE mc.id.shopCode = :shopCode AND mc.categoryName LIKE %:categoryName% ORDER BY mc.id.categoryCode ASC")
    List<MenuCategory> findByCategoryNameContainingAndShopCode(
        @Param("categoryName") String categoryName, @Param("shopCode") int shopCode);

    // 복합키로 존재 여부 확인
    boolean existsByIdCategoryCodeAndIdShopCode(int categoryCode, int shopCode);
}