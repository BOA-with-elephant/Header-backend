package com.header.header.common.controller;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("api/v1/my-shops/{shopId}")
public abstract class MyShopBaseController {

    protected Integer shopId;

    @ModelAttribute
    public void setShopId(@PathVariable("shopId") Integer shopId){
        this.shopId = shopId;
    }
}
