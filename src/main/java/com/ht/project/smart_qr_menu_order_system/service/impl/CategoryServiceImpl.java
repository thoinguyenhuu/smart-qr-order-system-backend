package com.ht.project.smart_qr_menu_order_system.service.impl;

import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateCategoryRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.UpdateCategoryRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.CategoryResponse;
import com.ht.project.smart_qr_menu_order_system.entity.Category;
import com.ht.project.smart_qr_menu_order_system.entity.Restaurant;
import com.ht.project.smart_qr_menu_order_system.exception.AppErrorCode;
import com.ht.project.smart_qr_menu_order_system.exception.AppException;
import com.ht.project.smart_qr_menu_order_system.repository.CategoryRepository;
import com.ht.project.smart_qr_menu_order_system.service.CategoryService;
import com.ht.project.smart_qr_menu_order_system.service.RestaurantAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Category management service for restaurant owner.
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final RestaurantAccessService restaurantAccessService;

    @Override
    @Transactional
    public CategoryResponse create(String restaurantId, CreateCategoryRequest request, String userId) {
        Restaurant restaurant = restaurantAccessService.getRestaurantForOwner(restaurantId, userId);
        Category category = Category.builder()
                .restaurant(restaurant)
                .name(request.name())
                .displayOrder(request.displayOrder())
                .build();
        category.setTenantId(restaurantId);
        return map(categoryRepository.save(category));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAll(String restaurantId, String userId) {
        restaurantAccessService.getRestaurantForMember(restaurantId, userId);
        return categoryRepository.findAllByRestaurantIdOrderByDisplayOrderAsc(restaurantId)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse getById(String restaurantId, String categoryId, String userId) {
        restaurantAccessService.getRestaurantForMember(restaurantId, userId);
        return map(categoryRepository.findByIdAndRestaurantId(categoryId, restaurantId)
                .orElseThrow(() -> new AppException(AppErrorCode.CATEGORY_NOT_FOUND)));
    }

    @Override
    @Transactional
    public CategoryResponse update(String restaurantId, String categoryId, UpdateCategoryRequest request, String userId) {
        restaurantAccessService.getRestaurantForOwner(restaurantId, userId);
        Category category = categoryRepository.findByIdAndRestaurantId(categoryId, restaurantId)
                .orElseThrow(() -> new AppException(AppErrorCode.CATEGORY_NOT_FOUND));
        category.setName(request.name());
        category.setDisplayOrder(request.displayOrder());
        return map(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void delete(String restaurantId, String categoryId, String userId) {
        restaurantAccessService.getRestaurantForOwner(restaurantId, userId);
        Category category = categoryRepository.findByIdAndRestaurantId(categoryId, restaurantId)
                .orElseThrow(() -> new AppException(AppErrorCode.CATEGORY_NOT_FOUND));
        categoryRepository.delete(category);
    }

    @Override
    public CategoryResponse getByName(String restaurantId, String name, String userId) {
        restaurantAccessService.getRestaurantForMember(restaurantId, userId);
        return map(categoryRepository.findByNameAndRestaurantId(name, restaurantId)
                .orElseThrow(() -> new AppException(AppErrorCode.CATEGORY_NOT_FOUND)));
    }

    private CategoryResponse map(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getRestaurant().getId(),
                category.getName(),
                category.getDisplayOrder()
        );
    }
}
