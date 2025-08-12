package com.header.header.domain.menu.repository;

import com.header.header.domain.menu.entity.Menu;
import com.header.header.domain.shop.projection.MenuSummaryWithRevCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    /* 메뉴 이름으로 메뉴 엔티티 조회하기 - 주혜 */
    @Query("SELECT m FROM Menu m WHERE m.menuName = :menuName AND m.menuCategory.id.shopCode = :shopCode")
    Menu findByMenuNameAndShopCode(@Param("menuName") String menuName, @Param("shopCode") Integer shopCode);
    /* 메뉴 이름으로 메뉴 엔티티 조회하기 */
    Menu findByMenuName(String menuName);

    /* 메뉴 코드, 샵 코드로 엔티티 조회하기 - 사용자 예약 생성 시 메뉴 유효성 체크 로직에서 사용*/
    @Query("""
           SELECT m 
           FROM Menu m 
           WHERE m.menuCode = :menuCode 
           AND m.menuCategory.id.shopCode = :shopCode
           AND m.isActive = true 
           """)
    Menu findByMenuCodeAndShopCodeAndIsActiveTrue(Integer menuCode, Integer shopCode);

    /**
     * 특정 카테고리의 메뉴들 활성화 상태 벌크 업데이트
     *
     * @param isActive 설정할 활성화 상태
     * @param categoryCode 카테고리 코드
     * @param shopCode 샵 코드
     * @param currentIsActive 현재 활성화 상태 (이 상태인 메뉴들만 업데이트)
     * @return 업데이트된 레코드 수
     */
    @Modifying
    @Query("UPDATE Menu m SET m.isActive = :isActive WHERE m.menuCategory.id.categoryCode = :categoryCode AND m.menuCategory.id.shopCode = :shopCode AND m.isActive = :currentIsActive")
    int updateActiveStatusByCategoryCodeAndShopCode(
        @Param("isActive") Boolean isActive,
        @Param("categoryCode") Integer categoryCode,
        @Param("shopCode") Integer shopCode,
        @Param("currentIsActive") Boolean currentIsActive);

    /*
    * 샵 검색시 메뉴 정보 (코드, 이름, 누적 예약 수) 가져오기
    *
    * @param shopCode 샵 코드
    * @return List<MenuSummaryDTO> 샵 코드, 메뉴 정보 (코드, 이름, 누적 예약 수)
    * */
    @Query("""
        SELECT 
                m.menuCategory.id.shopCode as shopCode,
                m.menuCode as menuCode,
                m.menuName as menuName,
                COUNT(r.resvCode) as menuRevCount
        FROM Menu m
        LEFT JOIN BossReservation r ON r.menuInfo.menuCode = m.menuCode
        WHERE m.menuCategory.id.shopCode IN :shopCode
        GROUP BY m.menuCategory.id.shopCode, m.menuCode, m.menuName
        ORDER BY menuRevCount DESC
        """)
    List<MenuSummaryWithRevCount> getMenuSummaryByShopCode(
            @Param("shopCode") List<Integer> shopCode
    );

}