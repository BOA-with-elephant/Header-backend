package com.header.header.domain.sales.repository;

import com.header.header.domain.sales.dto.SalesDetailDTO;
import com.header.header.domain.sales.entity.Sales;
import com.header.header.domain.sales.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import retrofit2.http.DELETE;

@Repository
public interface SalesRepository extends JpaRepository<Sales, Integer> {

    /**
     * 매출 상세 정보 조회 (모든 관련 테이블 JOIN) Sales + BossReservation + User + Menu + MenuCategory 정보를 한번에 조회
     */
    @Query("SELECT new com.header.header.domain.sales.dto.SalesDetailDTO(" +
        "s.salesCode, s.resvCode, s.payAmount, s.payMethod, s.payDatetime, " +
        "s.payStatus, s.cancelAmount, s.cancelDatetime, s.cancelReason, s.finalAmount, " +
        "br.shopInfo.shopCode, br.userInfo.userCode, br.menuInfo.menuCode, br.resvDate, br.resvTime, br.userComment, "
        +
        "br.userInfo.userName, br.userInfo.userPhone, br.menuInfo.menuName, br.menuInfo.menuPrice, "
        +
        "mc.menuColor, mc.categoryName) " +
        "FROM Sales s " +
        "JOIN BossReservation br ON s.resvCode = br.resvCode " +
        "JOIN br.menuInfo.menuCategory mc " +
        "WHERE s.salesCode = :salesCode")
    Optional<SalesDetailDTO> findSalesDetailById(@Param("salesCode") Integer salesCode);

    /**
     * 예약 코드로 매출 상세 정보 조회
     */
    @Query("SELECT new com.header.header.domain.sales.dto.SalesDetailDTO(" +
        "s.salesCode, s.resvCode, s.payAmount, s.payMethod, s.payDatetime, " +
        "s.payStatus, s.cancelAmount, s.cancelDatetime, s.cancelReason, s.finalAmount, " +
        "br.shopInfo.shopCode, br.userInfo.userCode, br.menuInfo.menuCode, br.resvDate, br.resvTime, br.userComment, "
        +
        "br.userInfo.userName, br.userInfo.userPhone, br.menuInfo.menuName, br.menuInfo.menuPrice, "
        +
        "mc.menuColor, mc.categoryName) " +
        "FROM Sales s " +
        "JOIN BossReservation br ON s.resvCode = br.resvCode " +
        "JOIN br.menuInfo.menuCategory mc " +
        "WHERE s.resvCode = :resvCode")
    Optional<SalesDetailDTO> findSalesDetailByResvCode(@Param("resvCode") Integer resvCode);

    /**
     * 특정 샵의 모든 매출 상세 정보 조회
     */
    @Query("SELECT new com.header.header.domain.sales.dto.SalesDetailDTO(" +
        "s.salesCode, s.resvCode, s.payAmount, s.payMethod, s.payDatetime, " +
        "s.payStatus, s.cancelAmount, s.cancelDatetime, s.cancelReason, s.finalAmount, " +
        "br.shopInfo.shopCode, br.userInfo.userCode, br.menuInfo.menuCode, br.resvDate, br.resvTime, br.userComment, "
        +
        "br.userInfo.userName, br.userInfo.userPhone, br.menuInfo.menuName, br.menuInfo.menuPrice, "
        +
        "mc.menuColor, mc.categoryName) " +
        "FROM Sales s " +
        "JOIN BossReservation br ON s.resvCode = br.resvCode " +
        "JOIN br.menuInfo.menuCategory mc " +
        "WHERE br.shopInfo.shopCode = :shopCode " +
        "ORDER BY s.payDatetime DESC")
    List<SalesDetailDTO> findSalesDetailsByShop(@Param("shopCode") Integer shopCode);

    /**
     * 특정 샵의 활성 매출 상세 목록 조회 (삭제 제외)
     *
     * @param shopCode 샵 코드
     * @return 활성 매출 상세 목록
     */
    @Query("SELECT new com.header.header.domain.sales.dto.SalesDetailDTO(" +
        "s.salesCode, s.resvCode, s.payAmount, s.payMethod, s.payDatetime, " +
        "s.payStatus, s.cancelAmount, s.cancelDatetime, s.cancelReason, s.finalAmount, " +
        "br.shopInfo.shopCode, br.userInfo.userCode, br.menuInfo.menuCode, br.resvDate, br.resvTime, br.userComment, "
        +
        "br.userInfo.userName, br.userInfo.userPhone, br.menuInfo.menuName, br.menuInfo.menuPrice, "
        +
        "mc.menuColor, mc.categoryName) " +
        "FROM Sales s " +
        "JOIN BossReservation br ON s.resvCode = br.resvCode " +
        "JOIN br.menuInfo.menuCategory mc " +
        "WHERE br.shopInfo.shopCode = :shopCode " +
        "AND s.payStatus != com.header.header.domain.sales.enums.PaymentStatus.DELETED " +
        "ORDER BY s.payDatetime DESC")
    List<SalesDetailDTO> findActiveSalesDetailsByShop(@Param("shopCode") Integer shopCode);

    /**
     * 특정 샵의 특정 상태 매출 상세 정보 조회
     */
    @Query("SELECT new com.header.header.domain.sales.dto.SalesDetailDTO(" +
        "s.salesCode, s.resvCode, s.payAmount, s.payMethod, s.payDatetime, " +
        "s.payStatus, s.cancelAmount, s.cancelDatetime, s.cancelReason, s.finalAmount, " +
        "br.shopInfo.shopCode, br.userInfo.userCode, br.menuInfo.menuCode, br.resvDate, br.resvTime, br.userComment, "
        +
        "br.userInfo.userName, br.userInfo.userPhone, br.menuInfo.menuName, br.menuInfo.menuPrice, "
        +
        "mc.menuColor, mc.categoryName) " +
        "FROM Sales s " +
        "JOIN BossReservation br ON s.resvCode = br.resvCode " +
        "JOIN br.menuInfo.menuCategory mc " +
        "WHERE br.shopInfo.shopCode = :shopCode AND s.payStatus = :payStatus " +
        "ORDER BY s.payDatetime DESC")
    List<SalesDetailDTO> findSalesDetailsByShopAndStatus(@Param("shopCode") Integer shopCode,
        @Param("payStatus") PaymentStatus payStatus);

    /**
     * 특정 샵의 기간별 매출 상세 정보 조회
     */
    @Query("SELECT new com.header.header.domain.sales.dto.SalesDetailDTO(" +
        "s.salesCode, s.resvCode, s.payAmount, s.payMethod, s.payDatetime, " +
        "s.payStatus, s.cancelAmount, s.cancelDatetime, s.cancelReason, s.finalAmount, " +
        "br.shopInfo.shopCode, br.userInfo.userCode, br.menuInfo.menuCode, br.resvDate, br.resvTime, br.userComment, "
        +
        "br.userInfo.userName, br.userInfo.userPhone, br.menuInfo.menuName, br.menuInfo.menuPrice, "
        +
        "mc.menuColor, mc.categoryName) " +
        "FROM Sales s " +
        "JOIN BossReservation br ON s.resvCode = br.resvCode " +
        "JOIN br.menuInfo.menuCategory mc " +
        "WHERE br.shopInfo.shopCode = :shopCode " +
        "AND s.payDatetime BETWEEN :startDate AND :endDate " +
        "ORDER BY s.payDatetime DESC")
    List<SalesDetailDTO> findSalesDetailsByShopAndDateRange(@Param("shopCode") Integer shopCode,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 샵의 모든 결제 조회 (JOIN 사용)
     */
    @Query("SELECT s FROM Sales s JOIN BossReservation br ON s.resvCode = br.resvCode WHERE br.shopInfo.shopCode = :shopCode")
    List<Sales> findByShopCode(@Param("shopCode") Integer shopCode);

    /**
     * 특정 샵의 결제 상태별 조회
     */
    @Query("SELECT s FROM Sales s JOIN BossReservation br ON s.resvCode = br.resvCode " +
        "WHERE br.shopInfo.shopCode = :shopCode AND s.payStatus = :payStatus")
    List<Sales> findByShopCodeAndPayStatus(@Param("shopCode") Integer shopCode,
        @Param("payStatus") PaymentStatus payStatus);

    /**
     * 특정 샵의 결제 일시 범위로 조회
     */
    @Query("SELECT s FROM Sales s JOIN BossReservation br ON s.resvCode = br.resvCode " +
        "WHERE br.shopInfo.shopCode = :shopCode AND s.payDatetime BETWEEN :startDate AND :endDate")
    List<Sales> findByShopCodeAndPayDatetimeBetween(@Param("shopCode") Integer shopCode,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 샵의 최종 금액 기준 조회
     */
    @Query("SELECT s FROM Sales s JOIN BossReservation br ON s.resvCode = br.resvCode " +
        "WHERE br.shopInfo.shopCode = :shopCode AND s.finalAmount >= :amount")
    List<Sales> findByShopCodeAndFinalAmountGreaterThan(@Param("shopCode") Integer shopCode,
        @Param("amount") Integer amount);

    @Query("SELECT s FROM Sales s JOIN BossReservation br ON s.resvCode = br.resvCode " +
        "WHERE br.shopInfo.shopCode = :shopCode AND s.finalAmount <= :amount")
    List<Sales> findByShopCodeAndFinalAmountLessThan(@Param("shopCode") Integer shopCode,
        @Param("amount") Integer amount);

    @Query("SELECT s FROM Sales s JOIN BossReservation br ON s.resvCode = br.resvCode " +
        "WHERE br.shopInfo.shopCode = :shopCode AND s.finalAmount BETWEEN :minAmount AND :maxAmount")
    List<Sales> findByShopCodeAndFinalAmountBetween(@Param("shopCode") Integer shopCode,
        @Param("minAmount") Integer minAmount,
        @Param("maxAmount") Integer maxAmount);

    /**
     * 특정 샵의 결제 방법별 조회
     */
    @Query("SELECT s FROM Sales s JOIN BossReservation br ON s.resvCode = br.resvCode " +
        "WHERE br.shopInfo.shopCode = :shopCode AND s.payMethod = :payMethod")
    List<Sales> findByShopCodeAndPayMethod(@Param("shopCode") Integer shopCode,
        @Param("payMethod") String payMethod);

    /**
     * 특정 샵의 취소된 결제 조회
     */
    @Query("SELECT s FROM Sales s JOIN BossReservation br ON s.resvCode = br.resvCode " +
        "WHERE br.shopInfo.shopCode = :shopCode AND s.cancelAmount > :amount")
    List<Sales> findByShopCodeAndCancelAmountGreaterThan(@Param("shopCode") Integer shopCode,
        @Param("amount") Integer amount);

    /**
     * 특정 샵의 취소 일시 범위로 조회
     */
    @Query("SELECT s FROM Sales s JOIN BossReservation br ON s.resvCode = br.resvCode " +
        "WHERE br.shopInfo.shopCode = :shopCode AND s.cancelDatetime BETWEEN :startDate AND :endDate")
    List<Sales> findByShopCodeAndCancelDatetimeBetween(@Param("shopCode") Integer shopCode,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 샵의 완료 상태인 결제만 조회
     */
    @Query("SELECT s FROM Sales s JOIN BossReservation br ON s.resvCode = br.resvCode " +
        "WHERE br.shopInfo.shopCode = :shopCode AND s.payStatus = :completedStatus")
    List<Sales> findCompletedPaymentsByShop(@Param("shopCode") Integer shopCode,
        @Param("completedStatus") PaymentStatus completedStatus);

    /**
     * 특정 샵의 취소된 결제 조회 (전체취소 + 부분취소)
     */
    @Query("SELECT s FROM Sales s JOIN BossReservation br ON s.resvCode = br.resvCode " +
        "WHERE br.shopInfo.shopCode = :shopCode AND (s.payStatus = :cancelled OR s.payStatus = :partialCancelled)")
    List<Sales> findCancelledPaymentsByShop(@Param("shopCode") Integer shopCode,
        @Param("cancelled") PaymentStatus cancelled,
        @Param("partialCancelled") PaymentStatus partialCancelled);

    /**
     * 특정 샵의 특정 기간 매출 합계
     */
    @Query(
        "SELECT SUM(s.finalAmount) FROM Sales s JOIN BossReservation br ON s.resvCode = br.resvCode "
            +
            "WHERE br.shopInfo.shopCode = :shopCode AND s.payDatetime BETWEEN :startDate AND :endDate "
            +
            "AND s.payStatus != :deletedStatus")
    Long calculateTotalSalesByShopBetween(@Param("shopCode") Integer shopCode,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate,
        @Param("deletedStatus") PaymentStatus deletedStatus);

    /**
     * 특정 샵의 특정 기간 취소 금액 합계
     */
    @Query(
        "SELECT SUM(s.cancelAmount) FROM Sales s JOIN BossReservation br ON s.resvCode = br.resvCode "
            +
            "WHERE br.shopInfo.shopCode = :shopCode AND s.cancelDatetime BETWEEN :startDate AND :endDate")
    Long calculateTotalCancelAmountByShopBetween(@Param("shopCode") Integer shopCode,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate);

    /**
     * 특정 샵의 결제 방법별 매출 통계
     */
    @Query("SELECT s.payMethod, SUM(s.finalAmount), COUNT(s) FROM Sales s " +
        "JOIN BossReservation br ON s.resvCode = br.resvCode " +
        "WHERE br.shopInfo.shopCode = :shopCode AND s.payStatus != :deletedStatus " +
        "GROUP BY s.payMethod")
    List<Object[]> findSalesStatsByPayMethodAndShop(@Param("shopCode") Integer shopCode,
        @Param("deletedStatus") PaymentStatus deletedStatus);

    /**
     * 특정 샵의 월별 매출 통계
     */
    @Query("SELECT YEAR(s.payDatetime), MONTH(s.payDatetime), SUM(s.finalAmount), COUNT(s) " +
        "FROM Sales s JOIN BossReservation br ON s.resvCode = br.resvCode " +
        "WHERE br.shopInfo.shopCode = :shopCode AND s.payStatus != :deletedStatus " +
        "GROUP BY YEAR(s.payDatetime), MONTH(s.payDatetime) " +
        "ORDER BY YEAR(s.payDatetime), MONTH(s.payDatetime)")
    List<Object[]> findMonthlySalesStatsByShop(@Param("shopCode") Integer shopCode,
        @Param("deletedStatus") PaymentStatus deletedStatus);

    /**
     * 특정 샵의 예약 존재 여부 확인
     */
    @Query("SELECT COUNT(s) > 0 FROM Sales s JOIN BossReservation br ON s.resvCode = br.resvCode " +
        "WHERE br.shopInfo.shopCode = :shopCode AND s.resvCode = :resvCode")
    boolean existsByShopCodeAndResvCode(@Param("shopCode") Integer shopCode,
        @Param("resvCode") Integer resvCode);

    /**
     * 특정 샵의 삭제되지 않은 결제만 조회
     */
    @Query("SELECT s FROM Sales s JOIN BossReservation br ON s.resvCode = br.resvCode " +
        "WHERE br.shopInfo.shopCode = :shopCode AND s.payStatus != :deletedStatus")
    List<Sales> findActivePaymentsByShop(@Param("shopCode") Integer shopCode,
        @Param("deletedStatus") PaymentStatus deletedStatus);

    /**
     * 특정 샵의 고액 결제 조회
     */
    @Query("SELECT s FROM Sales s JOIN BossReservation br ON s.resvCode = br.resvCode " +
        "WHERE br.shopInfo.shopCode = :shopCode AND s.payAmount >= :amount " +
        "AND s.payStatus != :deletedStatus ORDER BY s.payAmount DESC")
    List<Sales> findHighAmountPaymentsByShop(@Param("shopCode") Integer shopCode,
        @Param("amount") Integer amount,
        @Param("deletedStatus") PaymentStatus deletedStatus);

    // === 기존 메소드들 (전체 데이터용) ===

    /**
     * 결제 상태별 조회
     */
    List<Sales> findByPayStatus(PaymentStatus payStatus);

    /**
     * 결제 일시 범위로 조회
     */
    List<Sales> findByPayDatetimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * 최종 금액 이상으로 조회
     */
    List<Sales> findByFinalAmountGreaterThan(Integer amount);

    /**
     * 예약 코드로 결제 존재 여부 확인
     */
    boolean existsByResvCode(Integer resvCode);


}