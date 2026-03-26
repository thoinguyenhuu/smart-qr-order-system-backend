package com.ht.project.smart_qr_menu_order_system.controller;

import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateRestaurantRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.UpdateRestaurantRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.MenuResponse;
import com.ht.project.smart_qr_menu_order_system.dtos.response.RestaurantResponse;
import com.ht.project.smart_qr_menu_order_system.service.RestaurantService;
import com.ht.project.smart_qr_menu_order_system.utils.ApiResult;
import com.ht.project.smart_qr_menu_order_system.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Restaurant endpoints for owner management and public menu.
 */
@RestController
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("/restaurants")
@RequiredArgsConstructor
public class RestaurantController {
    RestaurantService restaurantService;

    @PostMapping
    ApiResult<RestaurantResponse> createRestaurant(@Valid @RequestBody CreateRestaurantRequest request) {
        return ApiResult.<RestaurantResponse>builder()
                .data(restaurantService.create(request))
                .message("Restaurant created successfully")
                .build();
    }

//    @GetMapping("/{restaurantId}/menu")
//    ApiResult<MenuResponse> getMenu(@PathVariable String restaurantId) {
//        return ApiResult.<MenuResponse>builder()
//                .data(restaurantService.getMenuOfRestaurant(restaurantId))
//                .build();
//    }

    @GetMapping
    ApiResult<java.util.List<RestaurantResponse>> getRestaurants() {
        return ApiResult.<java.util.List<RestaurantResponse>>builder()
                .data(restaurantService.getRestaurants(SecurityUtils.getCurrentUserId()))
                .build();
    }

    @GetMapping("/{restaurantId}")
    ApiResult<RestaurantResponse> getRestaurant(@PathVariable String restaurantId) {
        return ApiResult.<RestaurantResponse>builder()
                .data(restaurantService.getRestaurant(restaurantId, SecurityUtils.getCurrentUserId()))
                .build();
    }

    @PutMapping("/{restaurantId}")
    ApiResult<RestaurantResponse> updateRestaurant(@PathVariable String restaurantId, @Valid @RequestBody UpdateRestaurantRequest request) {
        return ApiResult.<RestaurantResponse>builder()
                .data(restaurantService.update(restaurantId, request, SecurityUtils.getCurrentUserId()))
                .message("Restaurant updated successfully")
                .build();
    }
}
