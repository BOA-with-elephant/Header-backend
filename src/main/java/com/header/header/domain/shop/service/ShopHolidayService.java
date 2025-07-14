package com.header.header.domain.shop.service;

import com.header.header.domain.reservation.repository.UserReservationRepository;
import com.header.header.domain.shop.dto.CreateHolReqDTO;
import com.header.header.domain.shop.dto.HolResDTO;
import com.header.header.domain.shop.entity.Shop;
import com.header.header.domain.shop.entity.ShopHoliday;
import com.header.header.domain.shop.enums.ShopErrorCode;
import com.header.header.domain.shop.exception.ShopExceptionHandler;
import com.header.header.domain.shop.repository.ShopHolidayRepository;
import com.header.header.domain.shop.repository.ShopRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.DayOfWeek;
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
    public HolResDTO createShopHoliday(CreateHolReqDTO dto) {

        /*비어있는지 검증한 shopCode 값 가져오기*/
        Integer shopCode = dto.getShopCode();

        /*존재하는 샵인지 검증*/
        Shop shop = shopRepository.findById(shopCode)
                .orElseThrow(() -> new ShopExceptionHandler(ShopErrorCode.SHOP_NOT_FOUND));

        /*해당 날짜에 예약이 있는지 확인*/
//        if (userReservationRepository) {}

        /*받은 데이터 빌드*/
        ShopHoliday hol = ShopHoliday
                .builder()
                .shopInfo(shop)
                .holStartDate(dto.getStartDate())
                .holEndDate(dto.getEndDate())
                .isHolRepeat(dto.isHolRepeat())
                .build();

        /*빌드한 데이터 저장*/
        ShopHoliday savedHol = shopHolidayRepository.save(hol);

        /*Entity -> DTO 변환하여 반환*/
        HolResDTO resDTO = modelMapper.map(savedHol, HolResDTO.class);

        /*정기 휴무일 경우 메시지*/
        if (dto.isHolRepeat()) {
            resDTO.setDescription("정기 휴무일이 정상적으로 반영되었습니다.");
        } else {
            resDTO.setDescription("휴무일이 정상적으로 반영되었습니다.");
        }

        return modelMapper.map(savedHol, HolResDTO.class);
    }

    /*사용자가 접근하려는 날짜가 휴일인지 검증하는 메소드*/
    public boolean isHoliday(Integer shopCode, Date dateToScan) {

        /*1. 임시 휴일 확인*/
        if (shopHolidayRepository.isTempHoliday(shopCode, dateToScan)) return true;

        /*2. 정기 휴일 확인*/
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

        /*3. 확인 메소드를 모두 통과한 경우 false = 휴일 아님 반환*/
        return false;
    }
}
