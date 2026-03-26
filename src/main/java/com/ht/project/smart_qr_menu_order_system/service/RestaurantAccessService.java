package com.ht.project.smart_qr_menu_order_system.service;

import com.ht.project.smart_qr_menu_order_system.entity.Restaurant;

/**
 * Helper service for validating restaurant membership and owner permissions.
 */
public interface RestaurantAccessService {
    Restaurant getRestaurant(String restaurantId);

    Restaurant getRestaurantForMember(String restaurantId, String userId);

    Restaurant getRestaurantForOwner(String restaurantId, String userId);
}
