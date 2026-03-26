package com.ht.project.smart_qr_menu_order_system.service.impl;

import com.ht.project.smart_qr_menu_order_system.entity.Restaurant;
import com.ht.project.smart_qr_menu_order_system.enums.Role;
import com.ht.project.smart_qr_menu_order_system.exception.AppErrorCode;
import com.ht.project.smart_qr_menu_order_system.exception.AppException;
import com.ht.project.smart_qr_menu_order_system.repository.RestaurantRepository;
import com.ht.project.smart_qr_menu_order_system.repository.RestaurantUserRepository;
import com.ht.project.smart_qr_menu_order_system.repository.UserRepository;
import com.ht.project.smart_qr_menu_order_system.service.RestaurantAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Centralized restaurant permission checks to avoid duplicating tenant logic.
 */
@Service
@RequiredArgsConstructor
public class RestaurantAccessServiceImpl implements RestaurantAccessService {
    private final RestaurantRepository restaurantRepository;
    private final RestaurantUserRepository restaurantUserRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Restaurant getRestaurant(String restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new AppException(AppErrorCode.RESTAURANT_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public Restaurant getRestaurantForMember(String restaurantId, String userId) {
        if (!restaurantUserRepository.existsByRestaurantIdAndUserId(restaurantId, userId)) {
            throw new AppException(AppErrorCode.ACCESS_DENIED);
        }
        return getRestaurant(restaurantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Restaurant getRestaurantForOwner(String restaurantId, String userId) {
        if (!restaurantUserRepository.existsByRestaurantIdAndUserId(restaurantId, userId)) {
            throw new AppException(AppErrorCode.ACCESS_DENIED);
        }
        if (userRepository.findById(userId).orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_FOUND)).getRole() != Role.OWNER) {
            throw new AppException(AppErrorCode.ACCESS_DENIED, "Only owner can manage this resource");
        }
        return getRestaurant(restaurantId);
    }
}
