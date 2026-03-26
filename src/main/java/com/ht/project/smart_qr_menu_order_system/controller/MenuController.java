package com.ht.project.smart_qr_menu_order_system.controller;

import com.ht.project.smart_qr_menu_order_system.dtos.response.MenuResponse;
import com.ht.project.smart_qr_menu_order_system.service.MenuService;
import com.ht.project.smart_qr_menu_order_system.utils.ApiResult;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/restaurants/{restaurantId}/menu")
public class MenuController {
    @Autowired
    MenuService menuService ;
    @PostMapping("/import-from-image")
    public ApiResult<Void> importMenu(@PathVariable String restaurantId,  @RequestParam MultipartFile file) throws IOException {
        menuService.generateMenuFromImage(restaurantId, file) ;
        return ApiResult.<Void>builder()
                .message("Đã tạo menu thành công").build() ;
    }

    @GetMapping
    ApiResult<MenuResponse> getMenu(@PathVariable String restaurantId) {
        return ApiResult.<MenuResponse>builder()
                .data(menuService.getMenuOfRestaurant(restaurantId))
                .build();
    }
}
