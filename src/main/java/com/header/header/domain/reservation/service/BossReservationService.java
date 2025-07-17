package com.header.header.domain.reservation.service;

import com.header.header.common.exception.NotFoundException;
import com.header.header.domain.menu.entity.Menu;
import com.header.header.domain.menu.repository.MenuRepository;
import com.header.header.domain.reservation.dto.*;
import com.header.header.domain.reservation.entity.BossReservation;
import com.header.header.domain.reservation.enums.ReservationState;
import com.header.header.domain.reservation.projection.BossResvDetailView;
import com.header.header.domain.reservation.repository.BossReservationRepository;
import com.header.header.domain.reservation.repository.UserReservationRepository;
import com.header.header.domain.sales.dto.SalesDTO;
import com.header.header.domain.sales.service.SalesService;
import com.header.header.domain.shop.entity.Shop;
import com.header.header.domain.shop.repository.ShopRepository;
import com.header.header.domain.user.dto.UserDTO;
import com.header.header.domain.user.entity.User;
import com.header.header.domain.user.repository.MainUserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BossReservationService {

    private final BossReservationRepository bossReservationRepository;
    private final UserReservationRepository userReservationRepository;
    private final MainUserRepository userRepository;
    private final ShopRepository shopRepository;
    private final MenuRepository menuRepository;
    private final SalesService salesService;
    private final ModelMapper modelMapper;

    /* 가게 예약 내역 전체 조회하기 */
    public List<BossResvProjectionDTO> findReservationList(Integer shopCode, String thisMonth){

        Date startDate = Date.valueOf(thisMonth + "-01");
        Date endDate = Date.valueOf(thisMonth + "-31");

        List<BossResvDetailView> reservationList = bossReservationRepository.findByShopCodeAndResvMonth(shopCode, startDate, endDate);

        return reservationList.stream()
                .map(reservation -> modelMapper.map(reservation, BossResvProjectionDTO.class))
                .toList();
    }

    /* 날짜별 가게 예약 내역 조회하기 */
    public List<BossResvProjectionDTO> findReservationListByDate(Integer shopCode, Date selectedDate){

        List<BossResvDetailView> reservationList = bossReservationRepository.findByShopCodeAndResvDate(shopCode, selectedDate);

        return reservationList.stream()
                .map(reservation -> modelMapper.map(reservation, BossResvProjectionDTO.class))
                .toList();
    }

    /* 고객명 별 가게 예약 내역 조회 */
    public List<BossResvProjectionDTO> findReservationListByName(Integer shopCode, String userName){

        List<BossResvDetailView> reservationList = bossReservationRepository.findByShopCodeAndUserName(shopCode, userName);

        return reservationList.stream()
                .map(reservation -> modelMapper.map(reservation, BossResvProjectionDTO.class))
                .toList();
    }

    /* 메뉴 이름 별 예약 내역 조회 */
    public List<BossResvProjectionDTO> findReservationListByMenuName(Integer shopCode, String menuName){

        List<BossResvDetailView> reservationList = bossReservationRepository.findByShopCodeAndMenuName(shopCode, menuName);

        return reservationList.stream()
                .map(reservation -> modelMapper.map(reservation, BossResvProjectionDTO.class))
                .toList();
    }

    /* 예약번호를 통한 예약 상세 조회 */
    public BossResvProjectionDTO findReservationByResvCode(Integer resvCode){

        BossResvDetailView reservation = bossReservationRepository.findByResvCode(resvCode)
                .orElseThrow(() -> NotFoundException.reservation(resvCode));

        return modelMapper.map(reservation, BossResvProjectionDTO.class);
    }

    /* 새 예약 등록하기 */
    public void registNewReservation(BossResvInputDTO inputDTO){
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

        BossReservationDTO registDTO = new BossReservationDTO();
        registDTO.setMenuInfo(new BossResvMenuDTO());
        registDTO.setUserInfo(new BossResvUserDTO());

        User newUser = null;

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

            newUser = userRepository.save(modelMapper.map(userDTO, User.class));
            registDTO.getUserInfo().setUserCode(newUser.getUserCode());
        }

        // reservationDTO에 입력받은 menuName과 shopCode로 menuCode 찾아서 넣기
        Menu menu = menuRepository.findByMenuNameAndShopCode(inputDTO.getMenuName(), inputDTO.getShopCode());
        registDTO.getMenuInfo().setMenuCode(menu.getMenuCode());

        // reservationDTO에 입력받은 resvDate, resvTime, userComment, resvState 넣기
        registDTO.setShopCode(inputDTO.getShopCode());
        registDTO.setResvDate(inputDTO.getResvDate());
        registDTO.setResvTime(inputDTO.getResvTime());
        registDTO.setUserComment(inputDTO.getUserComment());
        registDTO.setResvState(ReservationState.APPROVE);

        Shop shop = shopRepository.findById(inputDTO.getShopCode())
                .orElseThrow(() -> NotFoundException.shop(inputDTO.getShopCode()));

        // 저장
        BossReservation reservation = modelMapper.map(registDTO, BossReservation.class);
        reservation.setMenu(menu);
        reservation.setUser(user != null ? user : newUser);
        reservation.setShop(shop);
        bossReservationRepository.save(reservation);
    }

    /* 예약 내용 수정하기 */
    public void updateReservation(BossResvInputDTO inputDTO, Integer resvCode){
        /*
        * 예약 날짜, 예약 시간, 시술 메뉴, 사용자코멘트만 변경 가능
        * 순서
        * 1. 전달 받은 resvCode로 해당 데이터 가져오기.
        * 2. DB에서 넘어온 값은 menuCode로 받아오지만 클라이언트에서 전달 받은 값은 menuName로 받아온다.
        * 3. 클라이언트에게 전달받은 menuName으로 menuCode 조회하기
        * 4. 조회해온 menuCode를 basicReservationDTO에 넣기
        * 5. 그 외의 클라이언트에게 전달받은 값들도 basicReservationDTO에 넣기
        * 6. basicReservationDTO를 BossReservation 엔티티로 변환하여 DB에 넣기(수정)
        */

        // 1 ~ 2번
        BossReservation foundReservation = bossReservationRepository.findById(resvCode).orElseThrow(IllegalArgumentException::new);

        // 3번
        Menu menu = menuRepository.findByMenuNameAndShopCode(inputDTO.getMenuName(), inputDTO.getShopCode());

        if(menu == null){
            throw NotFoundException.menu(menu.getMenuCode());
        }

        // 4 ~ 5번
        BossReservationDTO registDTO = new BossReservationDTO();
        registDTO.setMenuInfo(new BossResvMenuDTO());
        registDTO.getMenuInfo().setMenuCode(menu.getMenuCode());
        registDTO.setResvDate(inputDTO.getResvDate());
        registDTO.setResvTime(inputDTO.getResvTime());
        registDTO.setUserComment(inputDTO.getUserComment());

        // 6번
        foundReservation.modifyReservation(registDTO, menu);
        // 이렇게 메소드 끝내면 엔티티에 변경 내용을 감지해서 자동으로 트랜잭션 처리된다.
    }


    /* 예약 취소하기 - 논리적 삭제 */
    public void cancelReservation(Integer resvCode){

        /* 예약 취소 시 물리적 삭제가 아닌 논리적 삭제로 진행하기 -> resvState를 예약 취소로 변경하기 */

        BossReservation foundReservation = bossReservationRepository.findById(resvCode).orElseThrow(IllegalArgumentException::new);

        if(foundReservation.getResvState() == ReservationState.APPROVE){
            foundReservation.cancelReservation();
        } else if (foundReservation.getResvState() == ReservationState.FINISH){
            throw new IllegalStateException("시술 완료건은 예약 취소가 불가합니다.");
        } else {
            throw new IllegalStateException("이미 예약 취소된 내역입니다.");
        }
    }

    /* 예약 내역 삭제 - 사장님이 매출에 영향을 끼쳐도 해당 내역을 삭제하고자 한다면 DB에서 물리적 삭제를 진행함 */
    public void deleteReservation(Integer resvCode){

        /*
        * 사장이 예약 내역 삭제 버튼 선택 시
        * 예약 내역을 삭제하면 "해당 내역을 삭제하면 매출에 영향을 미칠 수 있습니다. 삭제하시겠습니까?" 경고창 띄우기
        * 사장님이 그럼에도 불구하고 삭제한다는 버튼을 선택  / 취소 선택시(삭제 안하겠다) resvCode는 서버로 전달하지 않음
        * 클라이언트에서 서버로 resvCode 넘기기
        * 클라이언트에서 받은 resvCode로 해당 내역을 DB에서 삭제
        */
        bossReservationRepository.deleteById(resvCode);
    }

    /* 시술 후 사장님이 시술 완료로 상태 변경하면 매출 테이블로 해당 데이터 넘기기(insert) */
    public void afterProcedure(SalesDTO salesDTO){

        BossReservation reservation = bossReservationRepository.findById(salesDTO.getResvCode()).orElseThrow(IllegalArgumentException::new);

        /* 예약 날짜가 현재 시간보다 이전인지 확인 */
        Date resvDate = reservation.getResvDate();
        Time resvTime = reservation.getResvTime();
        LocalDateTime resvDateTime = LocalDateTime.of(resvDate.toLocalDate(), resvTime.toLocalTime());
        LocalDateTime now = LocalDateTime.now();

        /* 예약 날짜가 현재 시간보다 이전이거나 같으면 시술 완료 처리 및 매출 정보 넘기기 */
        if(reservation.getResvState() == ReservationState.APPROVE){
            if(resvDateTime.isBefore(now) || resvDateTime.isEqual(now)){
                /* 프론트에서 사장님이 시술 완료 버튼 클릭 시 해당 예약의 resvState가 예약 확정에서 시술 완료로 변경 */
                reservation.completeProcedure();

                salesService.createPayment(salesDTO);
            } else {
                /* 예외 처리 */
                throw new IllegalArgumentException("시술 완료 처리는 예약 시간이 지난 경우에만 가능합니다.");
            }
        } else if(reservation.getResvState() == ReservationState.FINISH){
            throw new IllegalStateException("이미 시술 완료된 내역입니다.");
        } else {
            throw new IllegalStateException("예약 취소 된 예약건은 시술 완료처리가 불가합니다.");
        }

    }

    /* 노쇼 갯수 반환 - 예약 확정 이면서 날짜가 오늘 이전인 것들 조회 */
    public List<BossResvProjectionDTO> findNoShowList(Date today, ReservationState resvState, Integer shopCode){

        List<BossResvDetailView> noShowList = bossReservationRepository.findByResvDateAndResvState(today, resvState, shopCode);

        return noShowList.stream()
                .map(noShow -> modelMapper.map(noShow, BossResvProjectionDTO.class))
                .toList();
    }

    /* 노쇼 처리 메소드 만들기 */
    public void noShowHandler(Integer resvCode){
        /*
        * 1. 사장님이 노쇼 리스트 조회 시 findNoShowList()가 실행됨
        * 2. 해당 리스트의 예약건을 노쇼 처리 버튼 클릭시 해당 메소드 noShowHandler() 실행됨
        * 3. 해당 예약건의 resvState는 예약 취소로 바꾸고, userComment는 "노쇼"라고 변경됨
        *
        * 일괄 처리 해야할까...?
        */
        LocalDate today = LocalDate.now();

        BossReservation noShow = bossReservationRepository.findById(resvCode).orElseThrow(IllegalArgumentException::new);

        // 애초에 노쇼 리스트에서 resvCode를 받아올거라 유효성 검사 안해도 되지만 혹시 몰라 진행함.
        if(noShow.getResvState() == ReservationState.APPROVE &&
                noShow.getResvDate().toLocalDate().isBefore(today)){
            noShow.noShowHandling();
        } else if(noShow.getUserComment().equals("노쇼")){
          throw new IllegalStateException("해당 예약은 이미 노쇼처리 되었습니다.");
        } else {
            throw new IllegalStateException("해당 예약 건은 노쇼가 아닙니다.");
        }
    }

    /* 테스트용 */
    public List<BossResvProjectionDTO> findReservationByUserNameAndUserPhone(Integer shopCode, String userName, String userPhone) {

        List<BossResvDetailView> reservationList = bossReservationRepository.findByUserNameAndUserPhone(shopCode, userName, userPhone);

        return reservationList.stream()
                .map(reservation -> modelMapper.map(reservation, BossResvProjectionDTO.class))
                .toList();
    }
}
