package com.ht.project.smart_qr_menu_order_system.service;

import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateCategoryRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.UpdateCategoryRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse create(String restaurantId, CreateCategoryRequest request, String userId);

    List<CategoryResponse> getAll(String restaurantId, String userId);

    CategoryResponse getById(String restaurantId, String categoryId, String userId);

    CategoryResponse update(String restaurantId, String categoryId, UpdateCategoryRequest request, String userId);

    void delete(String restaurantId, String categoryId, String userId);

    CategoryResponse getByName(String restaurantId, String name, String userId) ;
}
