package com.header.header.domain.visitors.repository;

import com.header.header.domain.visitors.enitity.Visitors;
import com.header.header.domain.visitors.projection.UserFavoriteMenuView;
import com.header.header.domain.visitors.projection.VisitStatisticsView;
import com.header.header.domain.visitors.projection.VisitorHistoryView;
import com.header.header.domain.visitors.projection.VisitorWithUserInfoView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VisitorsRepository extends JpaRepository<Visitors,Integer> {

    // (1) 기본 방문자 정보 리스트
    @Query("SELECT v.clientCode as clientCode, " +
            "       v.userCode as userCode, " +
            "       v.memo as memo, " +
            "       v.sendable as sendable, " +
            "       u.userName as userName, " +
            "       u.userPhone as userPhone, " +
            "       u.birthday as birthday " +
            "FROM Visitors v " +
            "INNER JOIN User u ON v.userCode = u.userCode " +
            "WHERE v.shopCode = :shopCode " +
            "AND v.isActive = true")
    List<VisitorWithUserInfoView> findVisitorWithUserInfoByShopCode(@Param("shopCode") Integer shopCode);

    // (2) 방문 및 결제 통계 및 마지막 방문일 리스트 조회
    @Query("SELECT r.userCode as userCode, " +
            "       COUNT(*) as visitCount, " +
            "       COALESCE(SUM(s.finalAmount), 0) as totalPaymentAmount, " +
            "       MAX(r.resvDate) as lastVisitDate " +
            "FROM Reservation r " +
            "INNER JOIN Sales s ON r.resvCode = s.resvCode " +
            "WHERE r.userCode IN :userCodes " +  // IN 절 사용
            "  AND r.resvState = '시술완료' " +
            "  AND s.finalAmount > 0 " +
            "GROUP BY r.userCode")
    List<VisitStatisticsView> getVisitStatisticsBatch(@Param("userCodes") List<Integer> userCodes);

    // (3) 방문 횟수 리스트 조회
    @Query("SELECT r.userCode as userCode, " +
            "       r.menuCode as menuCode, " +
            "       m.menuName as menuName, " +
            "       COUNT(*) as orderCount " +
            "FROM Reservation r " +
            "INNER JOIN Menu m ON r.menuCode = m.menuCode " +
            "WHERE r.userCode IN :userCodes " +
            "  AND r.resvState = '시술완료' " +
            "GROUP BY r.userCode, r.menuCode, m.menuName " +
            "ORDER BY r.userCode, COUNT(*) DESC")  // userCode별로 정렬, 주문수 내림차순
    List<UserFavoriteMenuView> getUserFavoriteMenusRaw(@Param("userCodes") List<Integer> userCodes);

    // (4) 샵 회원 히스토리 리스트 조회
    @Query("SELECT  r.resvDate as visitDate, " +
            "       m.menuName as menuName " +
            "FROM Visitors v " +
            "INNER JOIN User u ON v.userCode = u.userCode " +
            "INNER JOIN Reservation r ON u.userCode = r.userCode " +
            "INNER JOIN Menu m ON r.menuCode = m.menuCode " +
            "WHERE v.clientCode = :clientCode " +
            "  AND r.resvState = '시술완료' " +
            "ORDER BY r.resvDate DESC")
    List<VisitorHistoryView> getVisitHistoryByClientCode(@Param("clientCode") Integer clientCode);

    // (5) clientCode로 샵 회원 조회
    Optional<Visitors> findByClientCode(Integer clientCode);

    @Query("SELECT v.userCode FROM Visitors v WHERE v.clientCode = :clientCode")
    Optional<Integer> findUserCodeByClientCode(@Param("clientCode") Integer clientCode);

    @Query("""
        SELECT v
         FROM Visitors v
         WHERE v.userCode = :userCode
    """)
    Visitors findByUserCode(@Param("userCode") Integer userCode);
}
