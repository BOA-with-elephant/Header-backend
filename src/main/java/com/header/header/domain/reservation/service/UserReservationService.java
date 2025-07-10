package com.header.header.domain.reservation.service;

import com.header.header.domain.auth.model.repository.AuthUserRepository;
import com.header.header.domain.reservation.dto.UserReservationDTO;
import com.header.header.domain.reservation.dto.UserReservationSummaryDTO;
import com.header.header.domain.reservation.entity.Reservation;
import com.header.header.domain.reservation.enums.UserReservationErrorCode;
import com.header.header.domain.reservation.exception.UserReservationExceptionHandler;
import com.header.header.domain.reservation.projection.UserReservationSummary;
import com.header.header.domain.reservation.repository.UserReservationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserReservationService {

    private final UserReservationRepository userReservationRepository;
    private final ModelMapper modelMapper;

    //User 정보 위해 임시 처리
    private final AuthUserRepository userRepository;

    // CREATE
    @Transactional
    public UserReservationDTO createUserReservation(UserReservationDTO resvInfo) {

        Reservation reservation = modelMapper.map(resvInfo, Reservation.class);

        Reservation savedReservation = userReservationRepository.save(reservation);

        return modelMapper.map(savedReservation, UserReservationDTO.class);
    }

    //READ (단건 조회 - 상세 조회)
    public UserReservationDTO getReservationByResvCode(Integer ResvCode) {
        Reservation reservation = userReservationRepository.findById(ResvCode)
                .orElseThrow(() -> new UserReservationExceptionHandler(UserReservationErrorCode.RESV_NOT_FOUND));
        return modelMapper.map(reservation, UserReservationDTO.class);
    }

    //READ (전체 조회 - UserReservationSummaryDTO)
    public List<UserReservationSummary> findByUserCode(Integer userCode) {

        if (userRepository.findById(userCode).isEmpty()) {
            throw new UserReservationExceptionHandler(UserReservationErrorCode.USER_NOT_FOUND);
        }

        return userReservationRepository.findByUserCode(userCode);
    }

    //DELETE (논리적 삭제)
    @Transactional
    public UserReservationDTO deleteUserReservation(Integer resvCode) {
        Reservation reservation = userReservationRepository.findById(resvCode)
                .orElseThrow(() -> new UserReservationExceptionHandler(UserReservationErrorCode.RESV_NOT_FOUND));

        reservation.cancelReservation();

        userReservationRepository.save(reservation);

        return modelMapper.map(reservation, UserReservationDTO.class);
    }

}
