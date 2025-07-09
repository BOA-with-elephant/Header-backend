package com.header.header.domain.reservation.service;

import com.header.header.domain.reservation.dto.BossReservationDTO;
import com.header.header.domain.reservation.dto.BossResvInputDTO;
import com.header.header.domain.reservation.entity.BossReservation;
import com.header.header.domain.reservation.entity.Reservation;
import com.header.header.domain.reservation.repository.BossReservationRepository;
import com.header.header.domain.user.enitity.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BossReservationService {

    private final BossReservationRepository bossReservationRepository;
    private final ModelMapper modelMapper;

    /* 가게 예약 내역 전체 조회하기 */
    public List<BossReservationDTO> findReservationList(Integer shopCode){

        List<BossReservation> reservationList = bossReservationRepository.findByShopCode(shopCode);

        return reservationList.stream()
                .map(reservation -> modelMapper.map(reservation, BossReservationDTO.class))
                .toList();
    }

    /* 날짜별 가게 예약 내역 조회하기 */
    public List<BossReservationDTO> findReservationListByDate(Integer shopCode, Date selectedDate){

        List<BossReservation> reservationList = bossReservationRepository.findByShopCodeAndResvDate(shopCode, selectedDate);

        return reservationList.stream()
                .map(reservation -> modelMapper.map(reservation, BossReservationDTO.class))
                .toList();
    }

    /* 고객명 별 가게 예약 내역 조회 */
    public List<BossReservationDTO> findReservationListByName(Integer shopCode, String userName){

        List<BossReservation> reservationList = bossReservationRepository.findByShopCodeAndUserName(shopCode, userName);

        return reservationList.stream()
                .map(reservation -> modelMapper.map(reservation, BossReservationDTO.class))
                .toList();
    }

    /* 메뉴 이름 별 예약 내역 조회 */
    public List<BossReservationDTO> findReservationListByMenuName(Integer shopCode, String menuName){

        List<BossReservation> reservationList = bossReservationRepository.findByShopCodeAndMenuName(shopCode, menuName);

        return reservationList.stream()
                .map(reservation -> modelMapper.map(reservation, BossReservationDTO.class))
                .toList();
    }

    /* 예약번호를 통한 예약 상세 조회 */
    public BossReservationDTO findReservationByResvCode(Integer shopCode, Integer resvCode){

        BossReservation reservation = bossReservationRepository.findByResvCode(shopCode, resvCode);

        return modelMapper.map(reservation, BossReservationDTO.class);
    }

    /* 새 예약 등록하기 */
//    @Transactional
//    public void registNewReservation(BossResvInputDTO inputDTO){
//        /*
//        * 1. 예약 정보 입력받기(userName, userPhone, MenuName, resvDate, resvTime, userComment, resvState(예약 확정))
//        * 2. 입력받은 정보를 DTO에 담아서 받기
//        * 3. 입력 받은 정보 등록하기
//        * 3-1. 기존 회원 O -> userName과 userPhone을 기반으로 userCode 가져오기
//        *                    menuName을 기반으로 menuCode 가져오기
//        * 3-2. 기존 회원 X -> user 테이블에 userName, userPhone 등록 후 userCode 가져오기
//        *                    menuName을 기반으로 menuCode 가져오기
//        * 4. 가져온 값을 다시 DTO에 저장하기
//        */
//
//        // ModelMapper 설정
//        modelMapper.addMappings(new PropertyMap<BossResvInputDTO, BossReservationDTO>() {
//            protected void configure(){
//                map(source.getUserName(), destination.getUserInfo().getUserName());
//                map(source.getUserPhone(), destination.getUserInfo().getUserPhone());
//                map(source.getMenuName(), destination.getMenuInfo().getMenuName());
//                map(source.getResvDate(), destination.getResvDate());
//                map(source.getResvTime(), destination.getResvTime());
//                map(source.getUserComment(), destination.getUserComment());
//            }
//        });
//
//        // 실제 변환 작업 수행
//        BossReservationDTO reservationDTO = modelMapper.map(inputDTO, BossReservationDTO.class);
//        User userCode = bossReservationRepository.findByUserNameAndUserPhone(inputDTO.getUserName(), inputDTO.getUserPhone());
//    }

    /* 예약 내역 삭제하기 */
    @Transactional
    public void cancleReservation(Integer resvCode){

        bossReservationRepository.deleteById(resvCode);

    }


}
