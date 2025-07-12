package com.header.header.domain.shop.service;

import com.header.header.domain.shop.dto.ShopCategoryDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Transactional
public class ShopCategoryServiceTest {

    @Autowired
    private ShopCategoryService shopCategoryService;

    @Test
    @DisplayName("모든 카테고리를 순서대로 조회")
    void findAllCategories() {
        // when
        List<ShopCategoryDTO> result = shopCategoryService.findAllCategories();

        //then
        assertEquals("헤어샵", result.get(0).getCategoryName());
        assertEquals("네일샵", result.get(1).getCategoryName());
        assertEquals("에스테틱", result.get(4).getCategoryName());

        result.forEach(dto ->
                System.out.println(
                        dto.getCategoryCode() + " / " +
                                dto.getCategoryName()));
    }
}
