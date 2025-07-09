package com.header.header.domain.shop.service;

import com.header.header.domain.shop.dto.ShopDTO;
import com.header.header.domain.shop.dto.ShopSummaryDTO;
import com.header.header.domain.shop.dto.ShopUpdateDTO;
import com.header.header.domain.shop.entity.Shop;
import com.header.header.domain.shop.enums.ShopErrorCode;
import com.header.header.domain.shop.exception.*;
import com.header.header.domain.shop.external.MapService;
import com.header.header.domain.shop.repository.ShopRepository;
import com.header.header.domain.shop.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final ModelMapper modelMapper;
    private final MapService mapService;

    //User 정보 가져오기 위해 임시 처리
    private final UserRepository userRepository;

/*    private Integer adminCode;

    private Integer getAdminCode() {
        User testUser = userRepository.findById(1).orElseThrow();
        return adminCode;
    }*/

    //CREATE
    @Transactional
    public ShopDTO createShop(ShopDTO shopDTO) {

        // 테스트용 하드코딩
        String testAddress = "서울특별시 종로구 세종대로 175";
        System.out.println("테스트 주소: " + testAddress);

        Map<String, Double> coords = mapService.getCoordinatesFromAddress(testAddress);
        System.out.println("좌표 결과: " + coords);

        if (coords == null) {
            throw new ShopExceptionHandler(ShopErrorCode.LOCATION_NOT_FOUND);
        }

        System.out.println(shopDTO.getShopLocation());

        Map<String, Double> coords1 = mapService.getCoordinatesFromAddress(shopDTO.getShopLocation());
        if (coords == null) {
            throw new ShopExceptionHandler(ShopErrorCode.LOCATION_NOT_FOUND);
        }

        //Hibernate는 ID 필드가 null이어야 새로운 entity인 것을 알 수 있음
        shopDTO.setShopCode(null);

        //입력된 주소 기반으로 위도, 경도 세팅
        shopDTO.setShopLa(coords.get("shopLa"));
        shopDTO.setShopLong(coords.get("shopLong"));

        // DTO -> Entity 변환
        Shop shop = modelMapper.map(shopDTO, Shop.class);

        // Repository를 통해 Entity 저장
        Shop savedShop = shopRepository.save(shop);

        // 저장된 Entity -> DTO 변환하여 반환
        return modelMapper.map(savedShop, ShopDTO.class);
    }

    // READ (단건 조회 - 상세 조회)
    public ShopDTO getShopByShopCode(Integer shopCode) {
        Shop shop = shopRepository.findById(shopCode)
                .orElseThrow(() -> new ShopExceptionHandler(ShopErrorCode.SHOP_NOT_FOUND));
        return modelMapper.map(shop, ShopDTO.class);
    }

    //READ (전체 조회 - 요약정보 조회용 SummaryDTO 사용)
    public List<ShopSummaryDTO> findShopsSummaryByAdminCode(Integer adminCode) {
        return shopRepository.findShopsSummaryByAdminCode(adminCode);
    }

    //UPDATE
    @Transactional
    public Shop updateShop(Integer shopCode, ShopUpdateDTO shopInfo) {
        Shop shop = shopRepository.findById(shopCode)
                .orElseThrow(() -> new ShopExceptionHandler(ShopErrorCode.SHOP_NOT_FOUND));
        if (shop.getIsActive() == false) {
            throw new ShopExceptionHandler(ShopErrorCode.SHOP_DEACTIVATED);
        }
        modelMapper.map(shopInfo, shop);
        return shopRepository.save(shop);
    }

    //DELETE
    @Transactional
    public void deActiveShop(Integer shopCode) {
        Shop shop = shopRepository.findById(shopCode)
                .orElseThrow(() -> new ShopExceptionHandler(ShopErrorCode.SHOP_NOT_FOUND));

        ShopDTO shopDTO = getShopByShopCode(shopCode);

        if (!shopDTO.getIsActive()) {
            throw new ShopExceptionHandler(ShopErrorCode.SHOP_DEACTIVATED);
        }

        shopDTO.setIsActive(false);
        modelMapper.map(shopDTO, shop);
        shopRepository.save(shop);

        System.out.println("논리적 삭제 성공");
    }
}
