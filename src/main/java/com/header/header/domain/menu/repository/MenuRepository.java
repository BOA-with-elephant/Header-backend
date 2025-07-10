package com.header.header.domain.menu.repository;

import com.header.header.domain.menu.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 메뉴 엔티티에 대한 데이터 접근 계층
 */
@Repository
public interface MenuRepository extends JpaRepository<Menu, Integer> {

    /**
     * 특정 샵의 모든 메뉴 조회
     * - 활성화/비활성화 상태와 관계없이 모든 메뉴 반환
     *
     * @param shopCode 샵 코드 (Integer 타입)
     * @return 해당 샵의 모든 메뉴 목록
     */
    @Query("SELECT m FROM Menu m WHERE m.menuCategory.id.shopCode = :shopCode")
    List<Menu> findByShopCode(@Param("shopCode") Integer shopCode);

    /**
     * 특정 샵의 활성화된 메뉴만 조회
     * - isActive = true인 메뉴만 반환
     * - 실제 서비스에서 고객에게 노출되는 메뉴들
     *
     * @param shopCode 샵 코드 (Integer 타입)
     * @return 해당 샵의 활성화된 메뉴 목록
     */
    @Query("SELECT m FROM Menu m WHERE m.menuCategory.id.shopCode = :shopCode AND m.isActive = true")
    List<Menu> findActiveMenusByShop(@Param("shopCode") Integer shopCode);

    /**
     * 특정 카테고리의 활성화된 메뉴만 조회
     * - 카테고리별로 메뉴를 그룹핑하여 조회
     * - 활성화된 메뉴만 반환 (isActive = true)
     *
     * @param categoryCode 카테고리 코드 (Integer 타입)
     * @param shopCode 샵 코드 (Integer 타입)
     * @return 해당 카테고리의 활성화된 메뉴 목록
     */
    @Query("SELECT m FROM Menu m WHERE m.menuCategory.id.categoryCode = :categoryCode " +
        "AND m.menuCategory.id.shopCode = :shopCode AND m.isActive = true")
    List<Menu> findActiveMenusByCategory(@Param("categoryCode") Integer categoryCode,
        @Param("shopCode") Integer shopCode);

    /**
     * 메뉴 이름으로 검색 (특정 샵 내에서)
     * - 메뉴 이름에 검색어가 포함된 메뉴들을 조회
     * - 대소문자 구분 없이 부분 일치로 검색 (LIKE %searchTerm%)
     * - 활성화된 메뉴만 검색 결과에 포함
     *
     * @param menuName 검색할 메뉴 이름 (부분 일치)
     * @param shopCode 검색 대상 샵 코드 (Integer 타입)
     * @return 검색 조건에 맞는 메뉴 목록
     */
    @Query("SELECT m FROM Menu m WHERE m.menuCategory.id.shopCode = :shopCode " +
        "AND m.menuName LIKE %:menuName% AND m.isActive = true")
    List<Menu> findByMenuNameContainingAndShopCode(@Param("menuName") String menuName,
        @Param("shopCode") Integer shopCode);

    /*
     * 인기 메뉴 조회 (예약 수 기준):
     @Query("SELECT m FROM Menu m JOIN Reservation ri ON m.menuCode = ri.menu.menuCode " +
            "WHERE m.menuCategory.id.shopCode = :shopCode AND m.isActive = true " +
         "GROUP BY m ORDER BY COUNT(ri) DESC")
     List<Menu> findPopularMenusByShop(@Param("shopCode") Integer shopCode, Pageable pageable);*/

    /* 메뉴 이름으로 메뉴 엔티티 조회하기 */
    Menu findByMenuName(String menuName);
}