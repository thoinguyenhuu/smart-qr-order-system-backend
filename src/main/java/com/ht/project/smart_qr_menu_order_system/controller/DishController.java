package com.ht.project.smart_qr_menu_order_system.controller;

import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateDishRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.UpdateDishTodayStatusRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.UpdateDishRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.DishResponse;
import com.ht.project.smart_qr_menu_order_system.service.DishService;
import com.ht.project.smart_qr_menu_order_system.utils.ApiResult;
import com.ht.project.smart_qr_menu_order_system.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Dish management endpoints scoped by restaurant UUID.
 */
@RestController
@RequestMapping("/restaurants/{restaurantId}/dishes")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class DishController {
    DishService dishService;

    @PostMapping
    ApiResult<DishResponse> create(@PathVariable String restaurantId, @Valid @RequestBody CreateDishRequest request) {
        return ApiResult.<DishResponse>builder()
                .data(dishService.create(restaurantId, request, SecurityUtils.getCurrentUserId()))
                .message("Dish created successfully")
                .build();
    }

    @GetMapping
    ApiResult<List<DishResponse>> getAll(@PathVariable String restaurantId) {
        return ApiResult.<List<DishResponse>>builder()
                .data(dishService.getAll(restaurantId, SecurityUtils.getCurrentUserId()))
                .build();
    }

    @GetMapping("/{dishId}")
    ApiResult<DishResponse> getById(@PathVariable String restaurantId, @PathVariable String dishId) {
        return ApiResult.<DishResponse>builder()
                .data(dishService.getById(restaurantId, dishId, SecurityUtils.getCurrentUserId()))
                .build();
    }

    @PutMapping("/{dishId}")
    ApiResult<DishResponse> update(@PathVariable String restaurantId,
                                   @PathVariable String dishId,
                                   @Valid @RequestBody UpdateDishRequest request) {
        return ApiResult.<DishResponse>builder()
                .data(dishService.update(restaurantId, dishId, request, SecurityUtils.getCurrentUserId()))
                .message("Dish updated successfully")
                .build();
    }

    @PatchMapping("/{dishId}/today-status")
    ApiResult<DishResponse> updateTodayStatus(@PathVariable String restaurantId,
                                              @PathVariable String dishId,
                                              @Valid @RequestBody UpdateDishTodayStatusRequest request) {
        return ApiResult.<DishResponse>builder()
                .data(dishService.updateTodayStatus(restaurantId, dishId, request, SecurityUtils.getCurrentUserId()))
                .message("Dish status for today updated successfully")
                .build();
    }

    @DeleteMapping("/{dishId}")
    ApiResult<Void> delete(@PathVariable String restaurantId, @PathVariable String dishId) {
        dishService.delete(restaurantId, dishId, SecurityUtils.getCurrentUserId());
        return ApiResult.<Void>builder()
                .message("Dish deleted successfully")
                .build();
    }
}
