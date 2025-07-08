package com.header.header.domain.reservation.repository;

import com.header.header.domain.reservation.dto.UserReservationSummaryDTO;
import com.header.header.domain.reservation.entity.Reservation;
import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserReservationRepository extends JpaRepository<Reservation, Integer> {

    @Query("SELECT new com.header.header.domain.reservation.dto.UserReservationSummaryDTO(u.resvCode, u.userCode, u.shopCode, u.resvDate,u.resvState) FROM Reservation u WHERE u.userCode = :userCode")
    List<UserReservationSummaryDTO> findReservationSummaryByUserCode(@Param("userCode") Integer userCode);
}
