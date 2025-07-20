package com.header.header.domain.shop.service;

import com.header.header.domain.shop.dto.*;
import com.header.header.domain.shop.entity.Shop;
import com.header.header.domain.shop.entity.ShopCategory;
import com.header.header.domain.shop.enums.ShopErrorCode;
import com.header.header.domain.shop.exception.*;
import com.header.header.domain.shop.external.MapService;
import com.header.header.domain.shop.projection.ShopDetailResponse;
import com.header.header.domain.shop.projection.ShopSearchSummaryResponse;
import com.header.header.domain.shop.projection.ShopSummary;
import com.header.header.domain.shop.repository.ShopCategoryRepository;
import com.header.header.domain.shop.repository.ShopRepository;
import com.header.header.domain.user.entity.User;
import com.header.header.domain.user.repository.MainUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final ModelMapper modelMapper;
    private final MapService mapService;


    private final MainUserRepository userRepository;

    private final ShopCategoryRepository shopCategoryRepository;

    //CREATE 샵 생성
    public ShopDTO createShop(ShopCreationDTO dto) {
        /*카테고리 코드 유효성 체크*/
        ShopCategory category = shopCategoryRepository.findById(dto.getCategoryCode())
                .orElseThrow(() -> new ShopExceptionHandler(ShopErrorCode.SHOP_CATEGORY_NOT_FOUND));

        /*관리자 유효성 체크*/
        User admin = userRepository.findById(dto.getAdminCode())
                .orElseThrow(() -> new ShopExceptionHandler(ShopErrorCode.ADMIN_NOT_FOUND));

        /*getCoordinatesFromAddress -> getCoordinates -> MapService 호출하여 API에 담긴 정보 가져옴*/
        MapServiceDTO.Document document = getCoordinatesFromAddress(dto.getShopLocation());

        /*응답받은 document 정보로 shop 위도, 경도 세팅*/
        dto.setShopLa(document.getLatitude());
        dto.setShopLong(document.getLongitude());

        /*신규 샵 생성*/
        Shop shop = Shop.builder()
                .shopName(dto.getShopName())
                .categoryInfo(category)
                .adminInfo(admin)
                .shopName(dto.getShopName())
                .shopPhone(dto.getShopPhone())
                .shopLocation(dto.getShopLocation())
                .shopLong(dto.getShopLong())
                .shopLa(dto.getShopLa())
                .shopOpen(dto.getShopOpen())
                .shopClose(dto.getShopClose())
                .isActive(true)
                .build();

        return modelMapper.map(shopRepository.save(shop), ShopDTO.class);
    }

    /*사용자가 검색하는 경우, 페이징 처리된 요약 조회 메소드
      필터: 카테고리 or 키워드
      정렬: 기끼운 거리순*/
    public Page<ShopSearchSummaryResponse> findShopsByCondition(
            Double lat, Double lon, Integer categoryCode, String keyword, Pageable pageable
    ) {

        if (lat == null || lon == null) {
            lat = 37.5145;
            lon = 127.1067; //사용자가 위치를 허용하지 않았을 때의 기본값, 송파구 중심 좌표
        }

        if (categoryCode != null) {

        ShopCategory category = shopCategoryRepository.findById(categoryCode)
                .orElseThrow(() -> new ShopExceptionHandler(ShopErrorCode.SHOP_CATEGORY_NOT_FOUND));
        }

        return shopRepository.findShopsByCondition(lat, lon, categoryCode, keyword, pageable);
    }

    /*관리자 혹은 사용자의 상세조회*/
    public List<ShopDetailResponse> readShopDetailByShopCode(Integer shopCode) {

        Shop shop = shopRepository.findById(shopCode)
                // 존재하지 않는 샵 예외 처리
                .orElseThrow(() -> new ShopExceptionHandler(ShopErrorCode.SHOP_NOT_FOUND));

        // 비활성화된 샵 조회 시도 시 예외 처리
        if (!shop.getIsActive()) {
            throw new ShopExceptionHandler(ShopErrorCode.SHOP_DEACTIVATED);
        }

        return shopRepository.readShopDetailByShopCode(shopCode);
    }

    /*관리자가 본인이 보유한 샵을 요약 조회*/
    public List<ShopSummary> readShopSummaryByAdminCode(Integer adminCode) {

        if (userRepository.findById(adminCode).isEmpty()) {
            //존재하지 않는 UserCode 예외
            throw new ShopExceptionHandler(ShopErrorCode.ADMIN_NOT_FOUND);
        } else if (shopRepository.readShopSummaryByAdminCode(adminCode).isEmpty()) {
            //샵을 보유하지 않은 User의 접근 예외
            throw new ShopExceptionHandler(ShopErrorCode.SHOP_NOT_FOUND);
        }

        return shopRepository.readShopSummaryByAdminCode(adminCode);
    }

    /*보유한 샵을 수정*/
    @Transactional
    public ShopDTO updateShop(Integer shopCode, ShopUpdateDTO dto) {

        /*존재하지 않는 샵 예외*/
        Shop shop = shopRepository.findById(shopCode)
                .orElseThrow(() -> new ShopExceptionHandler(ShopErrorCode.SHOP_NOT_FOUND));

        /*필드별로 null 체크하여 기존 값 유지, 더티 체킹이 동작하지 않아 처리. 추후 검토 예정*/
        String shopName = dto.getShopName() != null ? dto.getShopName() : shop.getShopName();
        String shopPhone = dto.getShopPhone() != null ? dto.getShopPhone() : shop.getShopPhone();
        String shopLocation = dto.getShopLocation() != null ? dto.getShopLocation() : shop.getShopLocation();
        String shopOpen = dto.getShopOpen() != null ? dto.getShopOpen() : shop.getShopOpen();
        String shopClose = dto.getShopClose() != null ? dto.getShopClose() : shop.getShopClose();

        /*존재하지 않는 카테고리 유형 예외*/
        ShopCategory category = shop.getCategoryInfo();
        if (dto.getCategoryCode() != null) {
            category = shopCategoryRepository.findById(dto.getCategoryCode())
                    .orElseThrow(() -> new ShopExceptionHandler(ShopErrorCode.SHOP_CATEGORY_NOT_FOUND));
        }

        /*비활성화된 샵 예외*/
        if(!shop.getIsActive()) {
            throw new ShopExceptionHandler(ShopErrorCode.SHOP_DEACTIVATED);
        }

        Double shopLa = shop.getShopLa();
        Double shopLong = shop.getShopLong();

        if (dto.getShopLocation() != null && !shop.getShopLocation().equals(dto.getShopLocation())) {

            /*샵의 주소를 변경했을 경우에만 위도, 경도 값 업데이트*/
            MapServiceDTO.Document document = getCoordinatesFromAddress(dto.getShopLocation());
            shopLa = document.getLatitude();
            shopLong = document.getLongitude();
        }

        /*DB 수정, ShopStatus 삭제로 ShopStatus 처리 제외*/

        shop.updateShopInfo(
                category,
                shopName,
                shopPhone,
                shopLocation,
                shopLong,
                shopLa,
                shopOpen,
                shopClose
        );

        return modelMapper.map(shop,ShopDTO.class);
    }

    //DELETE (논리적 삭제)
    @Transactional
    public void deActiveShop(Integer shopCode) {
        Shop shop = shopRepository.findById(shopCode)
                .orElseThrow(() -> new ShopExceptionHandler(ShopErrorCode.SHOP_NOT_FOUND));

        if (!shop.getIsActive()) {
            throw new ShopExceptionHandler(ShopErrorCode.SHOP_ALREADY_DEACTIVATED);
        }

        shop.deactivateShop();

        shopRepository.save(shop);
    }

    /* MapService - getCoordinates 메소드를 통해 문서에서 필요한 정보를 가져오는 메소드
       - CREATE, UPDATE 시에 중복되는 코드라 이곳에 구현함

       @param address 변환할 주소
       @return MapServiceDTO.Document (위도, 경도 포함)
       @throws ShopErrorCode.LOCATION_NOT_FOUND (주소에 해당하는 좌표를 찾을 수 없을 경우 발생)
       */
    private MapServiceDTO.Document getCoordinatesFromAddress(String address){
        MapServiceDTO mapServiceDTO = mapService.getCoordinates(address).block();

        if(mapServiceDTO == null ||
                mapServiceDTO.getDocuments() == null ||
                mapServiceDTO.getDocuments().isEmpty() ||
                mapServiceDTO.getDocuments().get(0) == null) {

            // 잘못된 샵 정보를 입력하려고 시도할 경우 예외 발생
            throw new ShopExceptionHandler(ShopErrorCode.LOCATION_NOT_FOUND);
        }

        Double lat = mapServiceDTO.getDocuments().get(0).getLatitude();
        Double lon = mapServiceDTO.getDocuments().get(0).getLongitude();

        if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
            // ST_Distance_Sphere 함수 사용, 잘못된 위도, 경도 값 제한
            throw new ShopExceptionHandler(ShopErrorCode.LOCATION_NOT_FOUND);
        }

        return mapServiceDTO.getDocuments().get(0);
    }
}
