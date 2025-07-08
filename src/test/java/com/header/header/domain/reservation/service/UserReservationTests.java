package com.header.header.domain.reservation.service;

import com.header.header.domain.reservation.dto.UserReservationDTO;
import com.header.header.domain.reservation.dto.UserReservationSummaryDTO;
import com.header.header.domain.reservation.enums.UserReservationState;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
        List<UserReservationSummaryDTO> resvList = userReservationService.findUserReservationsByUserCode(userCode);

        //then
        System.out.println(resvList);
    }

    @Test
    @DisplayName("DELETE")
    @Commit
    public void testDeleteUserReservation() {
        //given
        Integer resvCode = 33;

        //when
        UserReservationDTO userReservationDTO = userReservationService.deleteUserReservation(resvCode);

        //then
        assert userReservationDTO.getResvState() == UserReservationState.CANCEL;
        System.out.println(userReservationDTO);
    }
}
