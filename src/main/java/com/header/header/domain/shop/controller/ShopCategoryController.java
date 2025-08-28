package com.header.header.domain.shop.controller;

import com.header.header.common.dto.response.ResponseMessage;
import com.header.header.common.dto.response.ShopApiResponse;
import com.header.header.domain.shop.dto.ShopAndMenuCategoryDTO;
import com.header.header.domain.shop.dto.ShopCategoryDTO;
import com.header.header.domain.shop.service.ShopCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "api/v1/shops", produces = "application/json; charset=UTF-8")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"},
        allowedHeaders = "*",
        methods = RequestMethod.GET)
public class ShopCategoryController {

    private final ShopCategoryService shopCategoryService;

    @GetMapping("/categories")
    public ResponseEntity<ResponseMessage> getShopCategories() {

        List<ShopCategoryDTO> shopCategories = shopCategoryService.findAllCategories();

        return ShopApiResponse.read("shop-categories", shopCategories);
    }

    @GetMapping("/shop-menu-categories")
    public ResponseEntity<ResponseMessage> getShopCategoryAndMenu() {

        ShopAndMenuCategoryDTO shopAndMenuCategories = shopCategoryService.findAllShopAndMenuCategories();

        return ShopApiResponse.read("categories", shopAndMenuCategories);
    }
}
