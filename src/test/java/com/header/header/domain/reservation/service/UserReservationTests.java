package com.header.header.domain.reservation.service;

import com.header.header.domain.reservation.dto.UserReservationDTO;
import com.header.header.domain.reservation.enums.ReservationState;
import com.header.header.domain.reservation.exception.UserReservationExceptionHandler;
import com.header.header.domain.reservation.projection.UserReservationSummary;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.sql.Date;
import java.sql.Time;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
public class UserReservationTests {

    @Autowired
    private UserReservationService userReservationService;

    @Test
    @DisplayName("CREATE")
    @Commit
    public void testCreateUserReservation() {

        //given
        Integer userCode = 1;
        Integer shopCode = 1;

        UserReservationDTO resvInfo = new UserReservationDTO();

        resvInfo.setUserCode(userCode);
        resvInfo.setShopCode(shopCode);
        resvInfo.setMenuCode(1);
        resvInfo.setResvDate(Date.valueOf("2022-01-01"));
        resvInfo.setResvTime(Time.valueOf("10:00:00"));
        resvInfo.setUserComment("양상추는 최고급으로 준비해주세요.");
        resvInfo.setResvState(ReservationState.APPROVE);

        //when
        UserReservationDTO createdResv = userReservationService.createUserReservation(resvInfo);

        //then
        assert createdResv.getUserCode() == userCode;
        System.out.println(createdResv);
    }

    @Test
    @DisplayName("단건 조회 - 상세 조회")
    public void testGetReservationByResvCode() {

        //given
        Integer resvCode = 1;

        //when
        UserReservationDTO resvInfo = userReservationService.getReservationByResvCode(resvCode);

        //then
        assert resvInfo.getResvCode() == resvCode;
        System.out.println(resvInfo);
    }

    @Test
    @DisplayName("전체 조회 - userCode로 조회")
    public void testFindUserReservationsByUserCode() {

        //given
        Integer userCode = 1;

        //when
        List<UserReservationSummary> resvList = userReservationService.findByUserCode(userCode);

        //then
        for (UserReservationSummary resv: resvList) {
            assert resv.getUserCode() == userCode;
            System.out.println(resv.getResvCode() + ", 유저코드 : " + resv.getUserCode() + ", 예약 상태 : " + resv.getResvState());
        }
    }

    @Test
    @DisplayName("전체 조회 예외 : 존재하지 않는 userCode로 조회")
    public void testUserNotFound() {

        //given
        Integer userCode = 200;

        //then
        assertThrows(UserReservationExceptionHandler.class, () -> userReservationService.findByUserCode(userCode));

    }



    @Test
    @DisplayName("DELETE")
    @Commit
    public void testCancelUserReservation() {
        //given
        Integer resvCode = 18;

        //when
        UserReservationDTO userReservationDTO = userReservationService.cancelReservation(resvCode);

        //then
        assert userReservationDTO.getResvState() == ReservationState.CANCEL;
        System.out.println(userReservationDTO);
    }

    @Test
    @DisplayName("DELETE 예외 : 결제 완료된 예약 취소 시도")
    public void testCancelPaidReservation() {
        //given
        Integer resvCode = 99;

        //when and then
        assertThrows(UserReservationExceptionHandler.class, () -> userReservationService.cancelReservation(resvCode));
    }

    @Test
    @DisplayName("DELETE 예외 : 시술 완료된 예약 취소 시도")
    public void testCancelWrongAttempt() {
        //given
        Integer resvCode = 8;

        //when and then
        assertThrows(UserReservationExceptionHandler.class, () -> userReservationService.cancelReservation(resvCode));
    }
    
    
}
