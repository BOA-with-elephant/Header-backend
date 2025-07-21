package com.header.header.domain.shop.repository;

import com.header.header.domain.shop.entity.Shop;
import com.header.header.domain.shop.projection.ShopDetailResponse;
import com.header.header.domain.shop.projection.ShopSearchSummaryResponse;
import com.header.header.domain.shop.projection.ShopSummary;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ShopRepository extends JpaRepository<Shop, Integer> {

    /*shop_code로 상세 조회*/
    @Query("""
        SELECT 
            s.shopCode as shopCode
          , s.shopName as shopName
          , s.shopPhone as shopPhone
          , s.shopLocation as shopLocation
          , s.shopOpen as shopOpen
          , s.shopClose as shopClose
          , sc.categoryName as categoryName
          , c.categoryName as menuCategoryName
          , m.menuCode as menuCode
          , m.menuName as menuName
          , m.menuPrice as menuPrice
          , m.estTime as estTime
                  
        FROM Shop s
        JOIN MenuCategory c ON s.shopCode = c.id.shopCode
        JOIN Menu m ON m.menuCategory.id.shopCode = c.id.shopCode
                    AND m.menuCategory.id.categoryCode = c.id.categoryCode
        JOIN ShopCategory sc ON s.categoryInfo.categoryCode = sc.categoryCode
                
        WHERE s.isActive = true
           AND c.isActive = true 
           AND s.shopCode = :shopCode
        """)
    List<ShopDetailResponse> readShopDetailByShopCode(Integer shopCode);

   /* 사용자가 샵을 검색할 때

      1) 키워드 (주소, 샵 이름)
      2) 카테고리 별로 검색
      3) 거리 오름차순 정렬

    거리 순 정렬에 사용되는 ST_Distance_Sphere() 함수가 필요해서 네이티브 쿼리 사용
    MySQL Dialect 설정을 시도했으나, 안정적이지 않아 계속 오류 발생
    */

    @Query(
            value = """
        SELECT 
            s.SHOP_CODE AS shopCode,
            s.SHOP_NAME AS shopName,
            s.SHOP_PHONE AS shopPhone,
            s.SHOP_LOCATION AS shopLocation,
            s.SHOP_LONG AS shopLong,
            s.SHOP_LA AS shopLa,
            sc.CATEGORY_NAME AS categoryName,
            ST_Distance_Sphere(
                    POINT(s.SHOP_LONG, s.SHOP_LA), 
                    POINT(:longitude, :latitude)) AS distance
                
        FROM TBL_SHOP s
        JOIN TBL_SHOP_CATEGORY sc ON s.CATEGORY_CODE = sc.CATEGORY_CODE
        WHERE (:categoryCode IS NULL OR s.CATEGORY_CODE = :categoryCode)
          AND (:keyword IS NULL OR s.SHOP_NAME LIKE %:keyword% OR s.SHOP_LOCATION LIKE %:keyword%)
          AND s.IS_ACTIVE = 1
        ORDER BY distance ASC
        """,
            countQuery = """
        SELECT COUNT(*)
        FROM TBL_SHOP s
        WHERE s.IS_ACTIVE = 1
          AND (:categoryCode IS NULL OR s.CATEGORY_CODE = :categoryCode)
          AND (:keyword IS NULL OR s.SHOP_NAME LIKE %:keyword% OR s.SHOP_LOCATION LIKE %:keyword%)
        """,
            nativeQuery = true
    )
    Page<ShopSearchSummaryResponse> findShopsByCondition(
            @Param("latitude") Double latitude,
            @Param("longitude") Double longitude,
            @Param("categoryCode") Integer categoryCode,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /* 관리자가 보유한 샵의 리스트 요약 조회 */
    @Query("""
    SELECT
        s.shopCode as shopCode,
        s.shopName as shopName,
        s.shopPhone as shopPhone,
        s.shopLocation as shopLocation,
        sc.categoryName as categoryName
    FROM Shop s
    JOIN ShopCategory sc ON s.categoryInfo.categoryCode = sc.categoryCode
    WHERE s.isActive = true
        AND s.adminInfo.userCode = :adminCode 
    """)
    List<ShopSummary> readShopSummaryByAdminCode(Integer adminCode);

    /*논리적 삭제시, 해당 코드를 가진 관리자의 샵이 맞는지 교차 검증하는 용도*/
    @Query("""
    SELECT s 
    FROM Shop s
    WHERE s.isActive = true 
    AND s.shopCode = :shopCode
    AND s.adminInfo.userCode = :adminCode
    """)
    Shop findByShopCodeAndAdminCode(
            @Param("shopCode") Integer shopCode,
            @Param("adminCode") Integer adminCode);


    /* 관리자가 샵을 비활성화하려고 시도할 경우, 샵이 한 개만 남아있다면 isAdmin = false로 전환된다 */
    @Query("""
    SELECT COUNT (s) = 1
    FROM Shop s
    WHERE s.adminInfo.userCode = :adminCode
    AND s.isActive = true
    """)
    boolean isShopLeft (@Param("adminCode") Integer adminCode);

}
