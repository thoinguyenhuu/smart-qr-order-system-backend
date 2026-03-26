package com.ht.project.smart_qr_menu_order_system.controller;

import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateCategoryRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.UpdateCategoryRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.CategoryResponse;
import com.ht.project.smart_qr_menu_order_system.service.CategoryService;
import com.ht.project.smart_qr_menu_order_system.utils.ApiResult;
import com.ht.project.smart_qr_menu_order_system.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Category management endpoints scoped by restaurant UUID.
 */
@RestController
@RequestMapping("/restaurants/{restaurantId}/categories")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CategoryController {
    CategoryService categoryService;

    @PostMapping
    ApiResult<CategoryResponse> create(@PathVariable String restaurantId, @Valid @RequestBody CreateCategoryRequest request) {
        return ApiResult.<CategoryResponse>builder()
                .data(categoryService.create(restaurantId, request, SecurityUtils.getCurrentUserId()))
                .message("Category created successfully")
                .build();
    }

    @GetMapping
    ApiResult<List<CategoryResponse>> getAll(@PathVariable String restaurantId) {
        return ApiResult.<List<CategoryResponse>>builder()
                .data(categoryService.getAll(restaurantId, SecurityUtils.getCurrentUserId()))
                .build();
    }

    @GetMapping("/{categoryId}")
    ApiResult<CategoryResponse> getById(@PathVariable String restaurantId, @PathVariable String categoryId) {
        return ApiResult.<CategoryResponse>builder()
                .data(categoryService.getById(restaurantId, categoryId, SecurityUtils.getCurrentUserId()))
                .build();
    }

    @PutMapping("/{categoryId}")
    ApiResult<CategoryResponse> update(@PathVariable String restaurantId,
                                       @PathVariable String categoryId,
                                       @Valid @RequestBody UpdateCategoryRequest request) {
        return ApiResult.<CategoryResponse>builder()
                .data(categoryService.update(restaurantId, categoryId, request, SecurityUtils.getCurrentUserId()))
                .message("Category updated successfully")
                .build();
    }

    @DeleteMapping("/{categoryId}")
    ApiResult<Void> delete(@PathVariable String restaurantId, @PathVariable String categoryId) {
        categoryService.delete(restaurantId, categoryId, SecurityUtils.getCurrentUserId());
        return ApiResult.<Void>builder()
                .message("Category deleted successfully")
                .build();
    }
}
