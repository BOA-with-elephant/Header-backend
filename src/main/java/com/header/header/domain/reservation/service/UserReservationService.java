package com.header.header.domain.reservation.service;

import com.header.header.domain.menu.entity.Menu;
import com.header.header.domain.menu.repository.MenuRepository;
import com.header.header.domain.reservation.dto.UserReservationDTO;
import com.header.header.domain.reservation.dto.UserReservationSearchConditionDTO;
import com.header.header.domain.reservation.dto.UserResvAvailableScheduleDTO;
import com.header.header.domain.reservation.entity.BossReservation;
import com.header.header.domain.reservation.enums.ReservationState;
import com.header.header.domain.reservation.enums.UserReservationErrorCode;
import com.header.header.domain.reservation.exception.UserReservationExceptionHandler;
import com.header.header.domain.reservation.projection.UserReservationDetail;
import com.header.header.domain.reservation.projection.UserReservationSummary;
import com.header.header.domain.reservation.repository.UserReservationRepository;
import com.header.header.domain.shop.entity.Shop;
import com.header.header.domain.shop.entity.ShopHoliday;
import com.header.header.domain.shop.repository.ShopHolidayRepository;
import com.header.header.domain.shop.repository.ShopRepository;
import com.header.header.domain.user.entity.User;
import com.header.header.domain.user.repository.MainUserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date; // util.Date -> 날짜 및 시간 (1970 기준 밀리초 포함), 오래된 범용 타입, 사용 지양
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserReservationService {

    private final UserReservationRepository userReservationRepository;
    private final ModelMapper modelMapper;
    private final MainUserRepository userRepository;
    private final ShopRepository shopRepository;
    private final MenuRepository menuRepository;
    private final ShopHolidayRepository shopHolidayRepository;

    /*사용자가 자신의 예약 내역을 상세 조회할 경우*/
    public Optional<UserReservationDetail> readDetailByUserCodeAndResvCode(Integer userCode, Integer resvCode) {

        /*사용자 정보가 존재하지 않을 경우 예외*/
        User user = userRepository.findById(userCode)
                .orElseThrow(() -> new UserReservationExceptionHandler(UserReservationErrorCode.USER_NOT_FOUND));

        if (user.isLeave()) {

            /*사용자가 탈퇴한 경우 예외*/
            throw new UserReservationExceptionHandler(UserReservationErrorCode.USER_HAS_LEFT);
        } else if (userReservationRepository.findById(resvCode).isEmpty()) {

            /*예약 정보가 존재하지 않을 경우 예외*/
            throw new UserReservationExceptionHandler(UserReservationErrorCode.RESV_NOT_FOUND);
        }

        return userReservationRepository.readDetailByUserCodeAndResvCode(userCode, resvCode);
    }

    /*사용자가 자신이 예약한 내역들의 목록을 조회할 경우*/
    public List<UserReservationSummary> findResvSummaryByUserCode(
            UserReservationSearchConditionDTO conditionDTO) {
        /*유효성 체크가 된 데이터들 한개씩 꺼내오기*/
        Integer userCode = conditionDTO.getUserCode();
        Date startDate = conditionDTO.getStartDate();
        Date endDate = conditionDTO.getEndDate();

        /*날짜 조회 필터를 사용할 경우*/
        if (startDate != null || endDate != null) {

            /*조회 시작 날짜가 조회 종료 날짜보다 이후인 경우 예외*/
            if (!startDate.before(endDate)) {
                throw new UserReservationExceptionHandler(UserReservationErrorCode.INPUT_DATE_WRONG);
            }
        }

        /*사용자 정보가 존재하지 않을 경우 예외*/
        User user = userRepository.findById(userCode)
                .orElseThrow(() -> new UserReservationExceptionHandler(UserReservationErrorCode.USER_NOT_FOUND));

        /*사용자가 탈퇴한 경우 예외*/
        if (user.isLeave()) {
            throw new UserReservationExceptionHandler(UserReservationErrorCode.USER_HAS_LEFT);
        }

        return userReservationRepository.findResvSummaryByUserCode(userCode, startDate, endDate);
    }

    /* 새로운 예약 생성, 반환값은 상세조회 프로젝션 인터페이스 사용 */
    @Transactional
    public Optional<UserReservationDetail> createReservation(
            Integer shopCode, UserReservationDTO dto
    ) {
        /* DTO 에서 어노테이션 유효성 검사 끝낸 값들 가져오기*/
        Integer userCode = dto.getUserCode();
        Integer menuCode = dto.getMenuCode();
        Date resvDate = dto.getResvDate();
        Time resvTime = dto.getResvTime();

        // 예약 시도 날짜가 샵 휴무일인 경우 예외
        if (isHoliday(shopCode, resvDate)) {
            throw new UserReservationExceptionHandler(UserReservationErrorCode.DATE_HAS_HOL);
        }

        // 예약 시도 날짜가 이미 예약되어 있을 경우 예외
        if (!userReservationRepository.isAvailableSchedule(shopCode, resvDate, resvTime)) {
            throw new UserReservationExceptionHandler(UserReservationErrorCode.SCHEDULE_ALREADY_TAKEN);
        }

        // 고객의 일정에 해당 날짜, 시간의 예약이 있을 경우 (노쇼 방지)
        if(userReservationRepository.isUserHasReservationInThisSchedule(userCode, resvDate, resvTime)) {
            throw new UserReservationExceptionHandler(UserReservationErrorCode.USER_SCHEDULE_UNAVAILABLE);
        }

        /*유효하지 않은 사용자 정보일 경우 예외*/
        User user = userRepository.findById(userCode)
                .orElseThrow(() -> new UserReservationExceptionHandler(UserReservationErrorCode.USER_NOT_FOUND));

        /*유효하지 않은 샵 정보일 경우 예외*/
        Shop shop = shopRepository.findById(shopCode)
                .orElseThrow(() -> new UserReservationExceptionHandler(UserReservationErrorCode.SHOP_NOT_FOUND));

        /*  유효하지 않는 메뉴 정보 예외
            1) 해당 샵이 가지고 있지 않은 메뉴인 경우
            2) 비활성화된 메뉴인 경우*/
        Menu menu = menuRepository.findByMenuCodeAndShopCodeAndIsActiveTrue(menuCode, shopCode);
        if (menu == null) {
            throw new UserReservationExceptionHandler(UserReservationErrorCode.MENU_NOT_FOUND);
        }

        BossReservation newReservation = BossReservation
                .builder()
                .userInfo(user)
                .shopInfo(shop)
                .menuInfo(menu)
                .resvDate(resvDate)
                .resvTime(resvTime)
                .userComment(dto.getUserComment())
                .resvState(ReservationState.APPROVE)
                // 예약을 생성할 경우 기본값은 "예약확정"
                .build();

        /* 예약 생성 */
        userReservationRepository.save(newReservation);

        return userReservationRepository.readDetailByUserCodeAndResvCode(userCode, newReservation.getResvCode());
    }

    /* 논리적 삭제 */
    @Transactional
    public void cancelReservation(Integer userCode, Integer resvCode) {

        /* 사용자 정보 유효성 검사 */
        User user = userRepository.findById(userCode)
                /*존재하지 않는 유저 정보 예외*/
                .orElseThrow(() -> new UserReservationExceptionHandler(UserReservationErrorCode.USER_NOT_FOUND));

        if (user.isLeave()) {
            /*탈퇴한 유저 정보 예외*/
            throw new UserReservationExceptionHandler(UserReservationErrorCode.USER_HAS_LEFT);
        }

        /*예약 정보 유효성 검사*/
        BossReservation reservation = userReservationRepository.findById(resvCode)

                /*존재하지 않는 예약 정보 예외*/
                .orElseThrow(() -> new UserReservationExceptionHandler(UserReservationErrorCode.RESV_NOT_FOUND));
        if (reservation.getResvState() == ReservationState.CANCEL) {

            /*이미 취소된 예약 정보 예외*/
            throw new UserReservationExceptionHandler(UserReservationErrorCode.RESV_ALREADY_DEACTIVATED);
        } else if (reservation.getResvState() == ReservationState.FINISH) {

            /*시술 완료된 예약 정보 예외*/
            throw new UserReservationExceptionHandler(UserReservationErrorCode.RESV_ALREADY_FINISHED);
        } else {

            /*위 유효성 검사를 모두 통과했을 경우, 엔티티 내부 취소 메소드 사용*/
            reservation.cancelReservation();
        }

        userReservationRepository.save(reservation);
    }

    /*사용자가 접근하려는 날짜가 휴일인지 검증하는 메소드
     * */
    public boolean isHoliday(Integer shopCode, Date dateToScan) {

        /*임시 휴일 확인 - 단 하루만 검사하는 쿼리 */
        if (shopHolidayRepository.isTempHoliday(shopCode, dateToScan)) return true;

        /*정기 휴일 확인 - 스캔하려는 날짜보다 이전에 설정된 휴일이 있는지 확인 */
        List<ShopHoliday> repeatHols = shopHolidayRepository.findRegHoliday(shopCode, dateToScan);

        /* 반환된 값이 비어있지 않을 때만 검증 */
        if (!repeatHols.isEmpty()) {
            /*java.time의 요일 계산 클래스*/
            DayOfWeek day = dateToScan.toLocalDate().getDayOfWeek();
            for (ShopHoliday hol : repeatHols) {
                if (hol.getHolStartDate().toLocalDate().getDayOfWeek() == day) {
                    return true;
                }
            }
        }

        /* 휴일 아님 false 반환*/
        return false;
    }

    /* 프론트에 예약 가능한 날짜와 시간을 모아서 보내주는 용도 */
    // @Param: shopCode, dateRangeToGet (for 문에서 스캔할 날짜의 개수... 일단 한 달 줄 예정)
    @Transactional(readOnly = true)
    // 영속성 컨텍스트에게 읽기 전용이라는 것을 명시해 성능 향상. 날짜별로 for문을 돌려서 쿼리가 많이 실행되기 때문에 방어용도로 사용
    public List<UserResvAvailableScheduleDTO> getAvailableSchedule(Integer shopCode, int dateRangeToGet) {

        //존재하지 않는 샵일 경우 예외
        Shop shop = shopRepository.findById(shopCode)
                .orElseThrow(() -> new UserReservationExceptionHandler(UserReservationErrorCode.SHOP_NOT_FOUND));

        List<UserResvAvailableScheduleDTO> result = new ArrayList<>();

        /* 데이터 검증 및 추가 순서
          @Param: shopOpen, shopClose, dateRangeToGet
        * 1) 현재를 기준으로 미래 한 달(dateRangeToGet) 까지 스캔해서 휴일이 없는 날짜
             - 현재 날짜+for문 돌리는 날짜, 샵 코드 필요
             - if(true) continue (휴일이 있다면, 현재 반복문 중단하고 다시 돌아가 조건에 맞으면 다음 단계 시행)

          2) 1 통과한 날짜들의 시간 한 시간 단위로 쪼개기

          3) 2 통과한 날짜, 시간 중 예약자가 없는 날짜와 시간
             - 1에서 통과한 날짜, 쪼개진 시간 필요
             - if (isAvailableSchedule == true) 인 경우만 list 추가
        * */

        for (int i = 0; i < dateRangeToGet; i++) {

            //for 문의 기준이 될 targetDate
            LocalDate targetDate = LocalDate.now().plusDays(i);

            // targetDate가 휴일인 경우 continue로 건너뛰고 다음 날짜 검증
            if (isHoliday(shopCode, Date.valueOf(targetDate))) continue;

            // 예약 가능한 시간들을 담을 list
            List<LocalTime> availableTimes = new ArrayList<>();

            /* String 타입인 샵 운영 시간 가져오기,
                shopOpen : 더해줄 시간 시작점
                shopClose : 시간 더하기 종료지점
                for문 내부에서 LocalDate.now(), plusMinutes() 사용하기 위해 LocalTime 사용 */
            LocalTime time = LocalTime.parse(shop.getShopOpen());
            LocalTime shopClose = LocalTime.parse(shop.getShopClose());

            // time 시간이 close 시간보다 이전일 동안
            while (time.isBefore(shopClose)) {

                // 예약 가능한 시간인지 검증
                boolean isAvailableSchedule = userReservationRepository
                        .isAvailableSchedule(shopCode, Date.valueOf(targetDate), Time.valueOf(time));

                if (isAvailableSchedule) {
                    // 예약 가능한 시간인 경우, 가능한 시간 list에 하나씩 더해준다
                    availableTimes.add(time);
                }
                // 예약은 한 시간 단위로 받는다
                time = time.plusMinutes(60);

            }
                // while 문을 빠져나왔을 때
                // 가능한 시간을 담은 리스트가 비어있지 않을 때만 UserResvAvailableScheduleDTO 객체 생성
                if (!availableTimes.isEmpty()) {
                    result.add(new UserResvAvailableScheduleDTO(targetDate, availableTimes));
                }

        }

        // for문 빠져나온 result 저장
        return result;
    }

}
