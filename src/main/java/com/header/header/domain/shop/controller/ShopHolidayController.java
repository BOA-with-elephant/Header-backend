package com.header.header.domain.shop.controller;

import com.header.header.domain.shop.service.ShopHolidayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/myshop/{shopCode}")
@CrossOrigin(origins = {"http://localhost:3000", "http://127.0.0.1:3000"},
        allowedHeaders = "*",
        methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
public class ShopHolidayController {

    private final ShopHolidayService shopHolidayService;

//    @GetMapping("/shops")
}
