package com.header.header.domain.shop.service;

import com.header.header.domain.shop.dto.ShopDTO;
import com.header.header.domain.shop.dto.ShopSummaryDTO;
import com.header.header.domain.shop.dto.ShopUpdateDTO;
import com.header.header.domain.shop.enitity.Shop;
import com.header.header.domain.shop.exception.ShopAlreadyDeletedException;
import com.header.header.domain.shop.exception.ShopNotFoundException;
import com.header.header.domain.shop.repository.ShopRepository;
import com.header.header.domain.shop.repository.UserRepository;
import com.header.header.domain.user.enitity.User;
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

    //User 정보 가져오기 위해 임시 처리
    private final UserRepository userRepository;

    private Integer adminCode;

    private Integer getAdminCode() {
        User testUser = userRepository.findById(1).orElseThrow();
        return adminCode;
    }

    //CREATE
    @Transactional
    public ShopDTO createShop(ShopDTO shopDTO) {
        //Hibernate는 ID 필드가 null이어야 새로운 entity인 것을 알 수 있음
        shopDTO.setShopCode(null);
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
                .orElseThrow(() -> new ShopNotFoundException("해당 샵을 찾을 수 없습니다."));
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
                .orElseThrow(() -> new ShopNotFoundException("해당 샵을 찾을 수 없습니다."));
        modelMapper.map(shopInfo, shop);
        return shopRepository.save(shop);
    }

    //DELETE
    @Transactional
    public void deActiveShop(Integer shopCode) {
        Shop shop = shopRepository.findById(shopCode)
                .orElseThrow(() -> new ShopNotFoundException("해당 샵을 찾을 수 없습니다."));

        ShopDTO shopDTO = getShopByShopCode(shopCode);

        if (shopDTO.getIsActive() == false) {
            throw new ShopAlreadyDeletedException("이미 비활성화된 샵입니다.");
        }
        shopDTO.setIsActive(false);

        modelMapper.map(shopDTO, shop);
        shopRepository.save(shop);

        if (shop.getIsActive() == false) {
            System.out.println("논리적 삭제 성공");
        } else if (shop.getIsActive() == true) {
            System.out.println("삭제 실패");
        }
    }
}
