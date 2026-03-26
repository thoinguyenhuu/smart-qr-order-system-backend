package com.ht.project.smart_qr_menu_order_system.service;

import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateRestaurantRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.UpdateRestaurantRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.MenuResponse;
import com.ht.project.smart_qr_menu_order_system.dtos.response.RestaurantResponse;

import java.util.List;

public interface RestaurantService {

    RestaurantResponse create(CreateRestaurantRequest request);

    RestaurantResponse update(String restaurantId, UpdateRestaurantRequest request, String userId);

    List<RestaurantResponse> getRestaurants(String userId);

    RestaurantResponse getRestaurant(String restaurantId, String userId);

//    MenuResponse getMenuOfRestaurant(String restaurantId);
}
