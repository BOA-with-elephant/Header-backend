package com.header.header.domain.shop.dto;

import com.header.header.domain.shop.projection.MenuCategoryForLLM;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ShopAndMenuCategoryDTO {

    private List<ShopCategoryDTO> shopCategories;

    private List<MenuCategoryForLLM> menuCategories;
}
