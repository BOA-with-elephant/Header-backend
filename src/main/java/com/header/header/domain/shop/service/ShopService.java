package com.header.header.domain.shop.service;

import com.header.header.domain.shop.dto.MapServiceDTO;
import com.header.header.domain.shop.dto.ShopDTO;
import com.header.header.domain.shop.entity.Shop;
import com.header.header.domain.shop.enums.ShopErrorCode;
import com.header.header.domain.shop.exception.*;
import com.header.header.domain.shop.external.MapService;
import com.header.header.domain.shop.projection.ShopSummary;
import com.header.header.domain.shop.repository.ShopRepository;
import com.header.header.domain.user.repository.MainUserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ShopService {

    private final ShopRepository shopRepository;
    private final ModelMapper modelMapper;
    private final MapService mapService;

    //User 정보 가져오기 위해 임시 처리 - 머지 & 삭제 후 임포트 구문 수정 예상
    private final MainUserRepository userRepository;

    //CREATE
    @Transactional
    public ShopDTO createShop(ShopDTO shopDTO) {
        // getCoordinatesFromAddress -> getCoordinates -> MapService 호출하여 API에 담긴 정보 가져옴
        MapServiceDTO.Document document = getCoordinatesFromAddress(shopDTO.getShopLocation());

        shopDTO.setShopLa(document.getLatitude());
        shopDTO.setShopLong(document.getLongitude());

        // Entity -> DTO 변환하여 반환
        return modelMapper.map(shopRepository.save(modelMapper.map(shopDTO, Shop.class)), ShopDTO.class);
    }

    // READ (단건 조회 - 상세 조회)
    public ShopDTO getShopByShopCode(Integer shopCode) {
        Shop shop = shopRepository.findById(shopCode)
                // 존재하지 않는 샵 예외 처리
                .orElseThrow(() -> new ShopExceptionHandler(ShopErrorCode.SHOP_NOT_FOUND));

        if (shop.getIsActive() == false) {
            // 비활성화된 샵 조회 시도 시 예외 처리 -> 테스트시 비활성화되지 않은 샵으로 해야 통과되니 주의
            throw new ShopExceptionHandler(ShopErrorCode.SHOP_DEACTIVATED);
        }
        return modelMapper.map(shop, ShopDTO.class);
    }

    //READ (전체 조회 - 요약정보 조회용 SummaryDTO 사용)
    public List<ShopSummary> findByAdminCodeAndIsActiveTrue(Integer adminCode) {

        if (userRepository.findById(adminCode).isEmpty()) {
            throw new ShopExceptionHandler(ShopErrorCode.ADMIN_NOT_FOUND);
        }

        List<ShopSummary> shopSummaryList = shopRepository.findByAdminCodeAndIsActiveTrue(adminCode);

        if(shopSummaryList.isEmpty()){
            throw new ShopExceptionHandler(ShopErrorCode.SHOP_NOT_FOUND);
        }

        return shopSummaryList;
    }

    //UPDATE
    @Transactional
    public ShopDTO updateShop(Integer shopCode, ShopDTO shopInfo) {
        Shop shop = shopRepository.findById(shopCode)
                .orElseThrow(() -> new ShopExceptionHandler(ShopErrorCode.SHOP_NOT_FOUND));

        if (shop.getIsActive() == false) {

            throw new ShopExceptionHandler(ShopErrorCode.SHOP_DEACTIVATED);
        } else if(!shop.getShopLocation().equals(shopInfo.getShopLocation())){

            /* 주소 설정을 위한 if문
               기존의 주소와 다를 경우에만 API 서버와 통신하여 DB의 위도, 경도 값을 업데이트 */
            // getCoordinatesFromAddress -> getCoordinates -> MapService 호출하여 API에 담긴 정보 가져옴
            MapServiceDTO.Document document = getCoordinatesFromAddress(shopInfo.getShopLocation());
            shopInfo.setShopLa(document.getLatitude());
            shopInfo.setShopLong(document.getLongitude());
        }

        // Entity -> DTO 변환하여 반환
        return modelMapper.map(shopRepository.save(modelMapper.map(shopInfo, Shop.class)), ShopDTO.class);
    }

    //DELETE (논리적 삭제)
    @Transactional
    public ShopDTO deActiveShop(Integer shopCode) {
        Shop shop = shopRepository.findById(shopCode)
                .orElseThrow(() -> new ShopExceptionHandler(ShopErrorCode.SHOP_NOT_FOUND));

        if (!shop.getIsActive()) {
            throw new ShopExceptionHandler(ShopErrorCode.SHOP_ALREADY_DEACTIVATED);
        }

        shop.deactivateShop();

        shopRepository.save(shop);

        return modelMapper.map(shop, ShopDTO.class);
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

        return mapServiceDTO.getDocuments().get(0);
    }
}
