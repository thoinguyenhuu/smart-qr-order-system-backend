package com.ht.project.smart_qr_menu_order_system.service;

import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateDishRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.UpdateDishTodayStatusRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.UpdateDishRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.DishResponse;

import java.util.List;

public interface DishService {
    DishResponse create(String restaurantId, CreateDishRequest request, String userId);

    List<DishResponse> getAll(String restaurantId, String userId);

    DishResponse getById(String restaurantId, String dishId, String userId);

    DishResponse update(String restaurantId, String dishId, UpdateDishRequest request, String userId);

    DishResponse updateTodayStatus(String restaurantId, String dishId, UpdateDishTodayStatusRequest request, String userId);

    void delete(String restaurantId, String dishId, String userId);
}
