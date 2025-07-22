package com.header.header.domain.shop.service;

import com.header.header.domain.reservation.repository.UserReservationRepository;
import com.header.header.domain.shop.dto.HolCreationDTO;
import com.header.header.domain.shop.dto.HolResDTO;
import com.header.header.domain.shop.dto.HolUpdateDTO;
import com.header.header.domain.shop.entity.Shop;
import com.header.header.domain.shop.entity.ShopHoliday;
import com.header.header.domain.shop.enums.ShopErrorCode;
import com.header.header.domain.shop.enums.ShopHolidayErrorCode;
import com.header.header.domain.shop.exception.ShopExceptionHandler;
import com.header.header.domain.shop.exception.ShopHolidayExceptionHandler;
import com.header.header.domain.shop.projection.ShopHolidayInfo;
import com.header.header.domain.shop.repository.ShopHolidayRepository;
import com.header.header.domain.shop.repository.ShopRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopHolidayService {

    private final ShopHolidayRepository shopHolidayRepository;
    private final ModelMapper modelMapper;

    /* 가게 정보 조회 */
    private final ShopRepository shopRepository;
    /* 예약 정보 조회 */
    private final UserReservationRepository userReservationRepository;

    /*새로운 휴일 규칙 생성*/
    @Transactional
    public HolResDTO createShopHoliday(Integer shopCode, HolCreationDTO dto) {

        /*DTO에서 검증한 값 가져오기*/
        Date startDate = dto.getStartDate();

        /*존재하는 샵인지 검증*/
        Shop shop = shopRepository.findById(shopCode)
                .orElseThrow(() -> new ShopExceptionHandler(ShopErrorCode.SHOP_NOT_FOUND));

        /*해당 날짜에 예약이 있는지 확인*/

        // 1) 일시적 휴일인 경우
        if (!dto.getIsHolRepeat()) {
            //밖에서 값 꺼내려고 시도하면 nullPointException
            Date endDate = dto.getEndDate();
            tempDayHasReserved(shopCode, startDate, endDate);
        } else {

            // 2) 반복하는 휴일인 경우
            /*
             * 프론트에서 로직
             * : 하루만 선택하는 경우 반복 가능, 혹은 요일반복/일정 세팅 선택지 크게 나누기
             * */

            if (regDayHasReserved(shopCode, startDate)) {
                // 검증 메소드가 true(예약건 있음) 반환하면 예외 처리
                throw new ShopHolidayExceptionHandler(ShopHolidayErrorCode.DATE_HAS_RESV);
            }

        }

        /*받은 데이터 빌드*/
        ShopHoliday hol = ShopHoliday
                .builder()
                .shopInfo(shop)
                .holStartDate(dto.getStartDate())
                .holEndDate(dto.getEndDate())
                .isHolRepeat(dto.getIsHolRepeat())
                .build();

        /*Entity 저장 -> DTO 변환하여 반환*/
        HolResDTO resDTO = modelMapper.map(shopHolidayRepository.save(hol), HolResDTO.class);

        /*정기 휴무일 경우 메시지*/
        if (dto.getIsHolRepeat()) {
            resDTO.setDescription("정기 휴무일이 정상적으로 반영되었습니다.");
        } else {
            resDTO.setDescription("휴무일이 정상적으로 반영되었습니다.");
        }

        return resDTO;
    }

    public HolResDTO updateShopHoliday(
            Integer shopCode, Integer shopHolCode, HolUpdateDTO dto
    ) {

        // validated data from dto
        Date startDate = dto.getStartDate();

        // ID로 기존 휴일 Entity를 조회
        ShopHoliday hol = shopHolidayRepository.findById(shopHolCode)
                .orElseThrow(() -> new ShopHolidayExceptionHandler(ShopHolidayErrorCode.HOL_NOT_FOUND));

        /* 해당 날짜에 예약이 있는지 확인, 생성일 때와 똑같음 */
        // 1) 일시적 휴일인 경우
        if (!dto.getIsHolRepeat()) {
            Date endDate = dto.getEndDate();
            tempDayHasReserved(shopCode, startDate, endDate);
        } else {

            // 2) 반복하는 휴일인 경우
            /*
             * 프론트에서 로직
             * : 하루만 선택하는 경우 반복 가능, 혹은 요일반복/일정 세팅 선택지 크게 나누기
             * */

            if (regDayHasReserved(shopCode, startDate)) {
                // 검증 메소드가 true(예약건 있음) 반환하면 예외 처리
                throw new ShopHolidayExceptionHandler(ShopHolidayErrorCode.DATE_HAS_RESV);
            }

        }

        // Entity에 비즈니스 로직을 위임하여 상태 변경
        hol.updateHolidayInfo(
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getIsHolRepeat()
        );

        // 변경된 Entity는 @Transactional에 의해 자동으로 DB에 반영(dirty checking)

        /*Entity 저장 -> DTO 변환하여 반환*/
        HolResDTO resDTO = modelMapper.map(shopHolidayRepository.save(hol), HolResDTO.class);

        /*정기 휴무일 경우 메시지*/
        if (dto.getIsHolRepeat()) {
            resDTO.setDescription("정기 휴무일이 정상적으로 반영되었습니다.");
        } else {
            resDTO.setDescription("휴무일이 정상적으로 반영되었습니다.");
        }

        return resDTO;
    }

    /*각각의 샵이 가진 휴일 정보를 불러옴*/
    public List<ShopHolidayInfo> getShopHolidayInfo(Integer shopCode) {

        LocalDate getToday = LocalDate.now();
        Date today = Date.valueOf(getToday); //오늘 날짜 구하기

        // 존재하지 않는 샵이면 예외
        Shop shop = shopRepository.findById(shopCode)
                .orElseThrow(() -> new ShopExceptionHandler(ShopErrorCode.SHOP_NOT_FOUND));

        return shopHolidayRepository.getShopHolidayInfo(shopCode, today);
    }

    // 휴일 정보 삭제, 물리적 삭제
    @Transactional
    public void deleteShopHoliday(Integer shopCode, Integer shopHolCode) {
        //존재하지 않는 휴일 정보일 경우 예외
        ShopHoliday hol = shopHolidayRepository.findById(shopHolCode)
                .orElseThrow(() -> new ShopHolidayExceptionHandler(ShopHolidayErrorCode.HOL_NOT_FOUND));

        //존재하지 않는 샵 정보 예외, 샵 코드가 더 데이터가 많은 테이블이기 때문에 ShopHoliday 먼저 검증 시도함
        shopRepository.findById(shopCode)
                .orElseThrow(() -> new ShopExceptionHandler(ShopErrorCode.SHOP_NOT_FOUND));

        // 그 샵에 해당하는 휴일 정보가 맞는지 검증
        if(!shopHolidayRepository.isHolReal(shopCode, shopHolCode)){
            throw new ShopHolidayExceptionHandler(ShopHolidayErrorCode.HOL_NOT_FOUND);
        }

        // jpa 기본 메소드로 삭제
        shopHolidayRepository.delete(hol);
    }

    /* 반복 휴일인 경우 해당 날짜에 예약이 있는지 확인하는 메소드 */
    boolean regDayHasReserved(Integer shopCode, Date startDate) {

        // 반복 휴일인 경우

        // 휴일 시작일의 요일 구하기 (DayOfWeek) 사용
        DayOfWeek dayOfWeek = startDate.toLocalDate().getDayOfWeek();

        // LocalDate의 메소드인 plusWeeks를 사용해 리스트에 저장한 후 검증 = LocalDate 사용한 이유
        List<LocalDate> dates = new ArrayList<>();
        LocalDate date = startDate.toLocalDate();
        for (int i = 0; i < 24; i++) {
            // 24주, 즉 6개월 반복 (예약은 6개월까지만 가능한 경우라고 가정)

            dates.add(date); //더한 값을 list에 저장

            date = date.plusWeeks(1); // 날짜에 1주씩 더해주는 메소드
        }

        for (LocalDate duplicated : dates) {

            /*리스트에 추가된 날짜에 예약이 있는지 확인*/
            boolean isExistRepeatHols = userReservationRepository
                    .isExistRepeatHols(shopCode, Date.valueOf(duplicated));

            //해당 날짜에 예약이 있으면 true
            if (isExistRepeatHols) {
                return true;
            }
        }

        // 반복문을 빠져나오면 false (해당 날짜에 예약된 건 없음)
        return false;

    }

    /*임시 휴무일 경우 검증하는 로직, 예약 던지지 않으면 원래 메소드로 돌아감*/
    void tempDayHasReserved(Integer shopCode, Date startDate, Date endDate) {

        /*시작 날짜가 종료 날짜 뒤에 있는 경우 검증*/
        if (startDate.after(endDate)) {
            throw new ShopHolidayExceptionHandler(ShopHolidayErrorCode.INPUT_DATE_WRONG);
        }

        /*예약 있으면 true*/
        boolean hasReservation = userReservationRepository
                .isExistDateBetweenHols(shopCode, startDate, endDate);

        if (hasReservation) {
            throw new ShopHolidayExceptionHandler(ShopHolidayErrorCode.DATE_HAS_RESV);
        }
    }
}
