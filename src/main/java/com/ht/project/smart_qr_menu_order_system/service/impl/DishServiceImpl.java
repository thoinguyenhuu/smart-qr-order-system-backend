package com.ht.project.smart_qr_menu_order_system.service.impl;

import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateDishRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.UpdateDishTodayStatusRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.UpdateDishRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.DishResponse;
import com.ht.project.smart_qr_menu_order_system.entity.Category;
import com.ht.project.smart_qr_menu_order_system.entity.Dish;
import com.ht.project.smart_qr_menu_order_system.exception.AppErrorCode;
import com.ht.project.smart_qr_menu_order_system.exception.AppException;
import com.ht.project.smart_qr_menu_order_system.repository.CategoryRepository;
import com.ht.project.smart_qr_menu_order_system.repository.DishRepository;
import com.ht.project.smart_qr_menu_order_system.service.DishService;
import com.ht.project.smart_qr_menu_order_system.service.RestaurantAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

/**
 * Dish management service for restaurant owner.
 */
@Service
@RequiredArgsConstructor
public class DishServiceImpl implements DishService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final DishRepository dishRepository;
    private final CategoryRepository categoryRepository;
    private final RestaurantAccessService restaurantAccessService;

    @Override
    @Transactional
    public DishResponse create(String restaurantId, CreateDishRequest request, String userId) {
        restaurantAccessService.getRestaurantForOwner(restaurantId, userId);
        Category category = categoryRepository.findByIdAndRestaurantId(request.categoryId(), restaurantId)
                .orElseThrow(() -> new AppException(AppErrorCode.CATEGORY_NOT_FOUND));

        Dish dish = Dish.builder()
                .category(category)
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .imageUrl(request.imageUrl())
                .available(request.available())
                .soldOutOn(null)
                .build();
        dish.setTenantId(restaurantId);
        return map(dishRepository.save(dish));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DishResponse> getAll(String restaurantId, String userId) {
        restaurantAccessService.getRestaurantForMember(restaurantId, userId);
        return dishRepository.findAllByCategoryRestaurantIdOrderByNameAsc(restaurantId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DishResponse getById(String restaurantId, String dishId, String userId) {
        restaurantAccessService.getRestaurantForMember(restaurantId, userId);
        return map(dishRepository.findByIdAndCategoryRestaurantId(dishId, restaurantId)
                .orElseThrow(() -> new AppException(AppErrorCode.DISH_NOT_FOUND)));
    }

    @Override
    @Transactional
    public DishResponse update(String restaurantId, String dishId, UpdateDishRequest request, String userId) {
        restaurantAccessService.getRestaurantForOwner(restaurantId, userId);
        Dish dish = dishRepository.findByIdAndCategoryRestaurantId(dishId, restaurantId)
                .orElseThrow(() -> new AppException(AppErrorCode.DISH_NOT_FOUND));
        dish.setName(request.name());
        dish.setDescription(request.description());
        dish.setPrice(request.price());
        dish.setImageUrl(request.imageUrl());
        dish.setAvailable(request.available());
        if (!Boolean.TRUE.equals(request.available())) {
            dish.setSoldOutOn(null);
        }
        return map(dishRepository.save(dish));
    }

    @Override
    @Transactional
    public DishResponse updateTodayStatus(String restaurantId, String dishId, UpdateDishTodayStatusRequest request, String userId) {
        restaurantAccessService.getRestaurantForMember(restaurantId, userId);
        Dish dish = dishRepository.findByIdAndCategoryRestaurantId(dishId, restaurantId)
                .orElseThrow(() -> new AppException(AppErrorCode.DISH_NOT_FOUND));

        if (Boolean.TRUE.equals(request.soldOutToday()) && !Boolean.TRUE.equals(dish.getAvailable())) {
            throw new AppException(AppErrorCode.BAD_REQUEST, "Món này đang tạm ngưng bán");
        }

        dish.setSoldOutOn(Boolean.TRUE.equals(request.soldOutToday()) ? LocalDate.now(BUSINESS_ZONE) : null);
        return map(dishRepository.save(dish));
    }

    @Override
    @Transactional
    public void delete(String restaurantId, String dishId, String userId) {
        restaurantAccessService.getRestaurantForOwner(restaurantId, userId);
        Dish dish = dishRepository.findByIdAndCategoryRestaurantId(dishId, restaurantId)
                .orElseThrow(() -> new AppException(AppErrorCode.DISH_NOT_FOUND));
        dishRepository.delete(dish);
    }

    private DishResponse map(Dish dish) {
        return new DishResponse(
                dish.getId(),
                dish.getCategory().getRestaurant().getId(),
                dish.getCategory().getId(),
                dish.getName(),
                dish.getDescription(),
                dish.getPrice(),
                dish.getImageUrl(),
                Boolean.TRUE.equals(dish.getAvailable()),
                dish.isSoldOutToday(BUSINESS_ZONE),
                dish.isAvailableForOrder(BUSINESS_ZONE)
        );
    }
}
