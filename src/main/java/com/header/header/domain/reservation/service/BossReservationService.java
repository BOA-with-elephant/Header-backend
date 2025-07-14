package com.header.header.domain.reservation.service;

import com.header.header.domain.menu.entity.Menu;
import com.header.header.domain.menu.repository.MenuRepository;
import com.header.header.domain.reservation.dto.BasicReservationDTO;
import com.header.header.domain.reservation.dto.BossReservationDTO;
import com.header.header.domain.reservation.dto.BossResvInputDTO;
import com.header.header.domain.reservation.entity.BossReservation;
import com.header.header.domain.reservation.entity.Reservation;
import com.header.header.domain.reservation.enums.ReservationState;
import com.header.header.domain.reservation.repository.BossReservationRepository;
import com.header.header.domain.reservation.repository.UserReservationRepository;
import com.header.header.domain.user.dto.UserDTO;
import com.header.header.domain.user.entity.User;
import com.header.header.domain.user.repository.MainUserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BossReservationService {

    private final BossReservationRepository bossReservationRepository;
    private final UserReservationRepository userReservationRepository;
    private final MainUserRepository userRepository;
    private final MenuRepository menuRepository;
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
    public BossReservationDTO findReservationByResvCode(Integer resvCode){

        BossReservation reservation = bossReservationRepository.findByResvCode(resvCode);

        if (reservation == null) {
            return null;
        }

        return modelMapper.map(reservation, BossReservationDTO.class);
    }

    /* 새 예약 등록하기 */
    public void  registNewReservation(BossResvInputDTO inputDTO){
        /*
        * 1. 예약 정보 입력받기(userName, userPhone, MenuName, resvDate, resvTime, userComment, resvState(예약 확정))
        * 2. 입력받은 정보를 DTO에 담아서 받기
        * 3. 입력 받은 정보 등록하기
        * 3-1. 기존 회원 O -> userName과 userPhone을 기반으로 userCode 가져오기
        *                    menuName을 기반으로 menuCode 가져오기
        * 3-2. 기존 회원 X -> user 테이블에 userName, userPhone 등록 후 userCode 가져오기
        *                    menuName을 기반으로 menuCode 가져오기
        * 4. 가져온 값을 다시 DTO에 저장하기
        */

//        ModelMapper modelMapper = new ModelMapper();
//        modelMapper.typeMap(BasicReservationDTO.class, BossReservation.class).addMappings(mapper -> {
//                    mapper.map(dto -> new Menu(dto.getMenuCode()), BossReservation::setMenuInfo);
//                    mapper.map(dto -> new User(dto.getUserCode()), BossReservation::setUserInfo);
//
//                }
//        );

//        BasicReservationDTO registDTO = new BasicReservationDTO();
        BossReservationDTO registDTO = new BossReservationDTO();

        // reservationDTO에 입력받은 userName과 userPhone으로 userCode 찾아서 넣기
        User user = userRepository.findByUserNameAndUserPhone(inputDTO.getUserName(), inputDTO.getUserPhone());


        if(user != null){
            // 기존 회원 번호 저장
            registDTO.getUserInfo().setUserCode(user.getUserCode());
        } else {
            // 새로운 회원 등록 후 해당 회원의 userCode 저장
            UserDTO userDTO = new UserDTO();
            userDTO.setUserName(inputDTO.getUserName());
            userDTO.setUserPhone(inputDTO.getUserPhone());
            userDTO.setIsAdmin(0);
            userDTO.setIsLeave(0);
            userRepository.save(modelMapper.map(userDTO, User.class));
            User newUser = userRepository.findByUserNameAndUserPhone(userDTO.getUserName(), userDTO.getUserPhone());
            registDTO.getUserInfo().setUserCode(newUser.getUserCode());
        }

        // reservationDTO에 입력받은 menuName으로 menuCode 찾아서 넣기
        Menu menu = menuRepository.findByMenuName(inputDTO.getMenuName());
        registDTO.getMenuInfo().setMenuCode(menu.getMenuCode());

        // reservationDTO에 입력받은 resvDate, resvTime, userComment, resvState 넣기
        registDTO.setShopCode(inputDTO.getShopCode());
        registDTO.setResvDate(inputDTO.getResvDate());
        registDTO.setResvTime(inputDTO.getResvTime());
        registDTO.setUserComment(inputDTO.getUserComment());
        registDTO.setResvState(ReservationState.APPROVE.name());

        // 저장
        bossReservationRepository.save(modelMapper.map(registDTO, BossReservation.class));
    }

    /* 예약 내용 수정하기 */
    public void updateReservation(BossResvInputDTO inputDTO, Integer resvCode){
        /*
        * 예약 날짜, 예약 시간, 시술 메뉴, 사용자코멘트만 변경 가능
        * 순서
        * 1. 전달 받은 resvCode로 해당 데이터 가져오기.
        * 2. DB에서 넘어온 값은 menuCode, userCode로 받아오지만 클라이언트에서 전달 받은 값은 userName, menuCode로 받아온다.
        * 3. 클라이언트에게 전달받은 menuName으로 menuCode 조회하기
        * 4. 각각 조회해온 userCode와 menuCode를 basicReservationDTO에 넣기
        * 5. 그 외의 클라이언트에게 전달받은 값들도 basicReservationDTO에 넣기
        * 6. basicReservationDTO를 reservation 엔티티로 변환하여 DB에 넣기(수정)
        */

        // 1 ~ 2번
        BossReservation foundReservation = userReservationRepository.findById(resvCode).orElseThrow(IllegalArgumentException::new);
        // Reservation -> BossReservation 컴파일에러 없애기 위해 수정

        // 3번
        Menu menu = menuRepository.findByMenuName(inputDTO.getMenuName());

        // 4 ~ 5번
        BasicReservationDTO registDTO = new BasicReservationDTO();
        registDTO.setMenuCode(menu.getMenuCode());
        registDTO.setResvDate(inputDTO.getResvDate());
        registDTO.setResvTime(inputDTO.getResvTime());
        registDTO.setUserComment(inputDTO.getUserComment());

        // 6번
        foundReservation.modifyReservation(registDTO);

        // 이렇게 메소드 끝내면 엔티티에 변경 내용을 감지해서 자동으로 트랜잭션 처리된다.
    }


//    /* 예약 내역 삭제하기 - 논리적 삭제 */
    public void cancelReservation(Integer resvCode){

        /* 예약 취소 시 물리적 삭제가 아닌 논리적 삭제로 진행하기 -> resvState를 예약 취소로 변경하기 */
        BossReservation foundReservation = userReservationRepository.findById(resvCode).orElseThrow(IllegalArgumentException::new);
        // Reservation -> BossReservation 컴파일에러 없애기 위해 수정

        foundReservation.cancelReservation();
    }


    /* 테스트용 */
    public List<BossReservationDTO> findReservationByUserNameAndUserPhone(Integer shopCode, String userName, String userPhone) {

        List<BossReservation> reservationList = bossReservationRepository.findByUserNameAndUserPhone(shopCode, userName, userPhone);

        return reservationList.stream()
                .map(reservation -> modelMapper.map(reservation, BossReservationDTO.class))
                .toList();
    }
}
