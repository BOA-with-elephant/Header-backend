package com.header.header.domain.reservation.repository;

import com.header.header.domain.reservation.entity.BossReservation;
import com.header.header.domain.reservation.entity.Reservation;
import com.header.header.domain.reservation.projection.UserReservationSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserReservationRepository extends JpaRepository<BossReservation, Integer> {

    // 요약 정보(UserReservationSummary)를 위해 Projection Based Interface 방식 사용
    List<UserReservationSummary> findByUserCode(Integer userCode);
}
