package com.header.header.domain.shop.service;

import com.header.header.domain.shop.dto.ShopCategoryDTO;
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

    public List<ShopCategoryDTO> findAllCategories() {
        return shopCategoryRepository.findAllByOrderByCategoryCodeAsc().stream()
                .map(category -> modelMapper.map(category, ShopCategoryDTO.class))
                .collect(Collectors.toList());
    }
}
