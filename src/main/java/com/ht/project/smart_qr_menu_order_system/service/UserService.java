package com.ht.project.smart_qr_menu_order_system.service;

import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateUserRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.UserResponse;

import java.util.List;

public interface UserService {
    /**
     * Owner creates cashier account for a restaurant.
     */
    UserResponse createUser(String restaurantId, CreateUserRequest request, String ownerId);

    /**
     * Owner deletes cashier account from a restaurant.
     */
    void deleteUser(String restaurantId, String id, String ownerId);

    /**
     * List members of a restaurant.
     */
    List<UserResponse> getUsers(String restaurantId, String ownerId);
}
