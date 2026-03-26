package com.ht.project.smart_qr_menu_order_system.service.impl;

import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateRestaurantRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.UpdateRestaurantRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.MenuCategoryResponse;
import com.ht.project.smart_qr_menu_order_system.dtos.response.MenuDishResponse;
import com.ht.project.smart_qr_menu_order_system.dtos.response.MenuResponse;
import com.ht.project.smart_qr_menu_order_system.dtos.response.RestaurantResponse;
import com.ht.project.smart_qr_menu_order_system.entity.Category;
import com.ht.project.smart_qr_menu_order_system.entity.Dish;
import com.ht.project.smart_qr_menu_order_system.entity.Restaurant;
import com.ht.project.smart_qr_menu_order_system.entity.RestaurantUser;
import com.ht.project.smart_qr_menu_order_system.entity.User;
import com.ht.project.smart_qr_menu_order_system.enums.Role;
import com.ht.project.smart_qr_menu_order_system.exception.AppErrorCode;
import com.ht.project.smart_qr_menu_order_system.exception.AppException;
import com.ht.project.smart_qr_menu_order_system.repository.CategoryRepository;
import com.ht.project.smart_qr_menu_order_system.repository.DishRepository;
import com.ht.project.smart_qr_menu_order_system.repository.RestaurantRepository;
import com.ht.project.smart_qr_menu_order_system.repository.RestaurantUserRepository;
import com.ht.project.smart_qr_menu_order_system.repository.UserRepository;
import com.ht.project.smart_qr_menu_order_system.service.RestaurantAccessService;
import com.ht.project.smart_qr_menu_order_system.service.RestaurantService;
import com.ht.project.smart_qr_menu_order_system.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Restaurant service for owner management and public menu querying.
 */
@Service
@RequiredArgsConstructor
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;
    private final RestaurantUserRepository restaurantUserRepository;
    private final CategoryRepository categoryRepository;
    private final DishRepository dishRepository;
    private final RestaurantAccessService restaurantAccessService;

    @Override
    @Transactional
    public RestaurantResponse create(CreateRestaurantRequest request) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        User owner = userRepository.findById(currentUserId)
                .orElseThrow(() -> new AppException(AppErrorCode.USER_NOT_FOUND));

        if (owner.getRole() != Role.OWNER) {
            throw new AppException(AppErrorCode.ACCESS_DENIED, "Only owner can create restaurant");
        }

        Restaurant restaurant = Restaurant.builder()
                .name(request.name())
                .address(request.address())
                .owner(owner)
                .build();

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        RestaurantUser restaurantUser = RestaurantUser.builder()
                .restaurant(savedRestaurant)
                .user(owner)
                .build();
        restaurantUser.setTenantId(savedRestaurant.getId());
        restaurantUserRepository.save(restaurantUser);

        return new RestaurantResponse(savedRestaurant.getId(), savedRestaurant.getName(), savedRestaurant.getAddress());
    }

    @Override
    @Transactional
    public RestaurantResponse update(String restaurantId, UpdateRestaurantRequest request, String userId) {
        Restaurant restaurant = restaurantAccessService.getRestaurantForOwner(restaurantId, userId);
        restaurant.setName(request.name());
        restaurant.setAddress(request.address());
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return new RestaurantResponse(savedRestaurant.getId(), savedRestaurant.getName(), savedRestaurant.getAddress());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantResponse> getRestaurants(String userId) {
        return restaurantUserRepository.findAllByUserId(userId)
                .stream()
                .map(RestaurantUser::getRestaurant)
                .map(restaurant -> new RestaurantResponse(restaurant.getId(), restaurant.getName(), restaurant.getAddress()))
                .distinct()
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantResponse getRestaurant(String restaurantId, String userId) {
        Restaurant restaurant = restaurantAccessService.getRestaurantForMember(restaurantId, userId);
        return new RestaurantResponse(restaurant.getId(), restaurant.getName(), restaurant.getAddress());
    }

}
