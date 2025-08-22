package com.header.header.domain.shop.service;

import com.header.header.domain.menu.repository.MenuCategoryRepository;
import com.header.header.domain.shop.dto.ShopAndMenuCategoryDTO;
import com.header.header.domain.shop.dto.ShopCategoryDTO;
import com.header.header.domain.shop.projection.MenuCategoryForLLM;
import com.header.header.domain.shop.repository.ShopCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ShopCategoryService {

    private final ShopCategoryRepository shopCategoryRepository;
    private final ModelMapper modelMapper;

    private final MenuCategoryRepository menuCategoryRepository;

    public List<ShopCategoryDTO> findAllCategories() {
        return shopCategoryRepository.findAllByOrderByCategoryCodeAsc().stream()
                .map(category -> modelMapper.map(category, ShopCategoryDTO.class))
                .collect(Collectors.toList());
    }

    public ShopAndMenuCategoryDTO findAllShopAndMenuCategories() {

        ShopAndMenuCategoryDTO shopAndMenuCategories = new ShopAndMenuCategoryDTO();

        List<ShopCategoryDTO> shopCategories = shopCategoryRepository.findAllByOrderByCategoryCodeAsc().stream()
                .map(category -> modelMapper.map(category, ShopCategoryDTO.class))
                .collect(Collectors.toList());

        List<MenuCategoryForLLM> menuCategories = menuCategoryRepository.findMenuCategoryForLLM();

        shopAndMenuCategories.setShopCategories(shopCategories);
        shopAndMenuCategories.setMenuCategories(menuCategories);

        return shopAndMenuCategories;
    }
}
