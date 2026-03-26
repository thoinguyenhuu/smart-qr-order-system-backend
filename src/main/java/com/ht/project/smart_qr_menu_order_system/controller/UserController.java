package com.ht.project.smart_qr_menu_order_system.controller;

import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateUserRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.UserResponse;
import com.ht.project.smart_qr_menu_order_system.service.UserService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Manage restaurant members such as cashier accounts.
 */
@RestController
@RequestMapping("/restaurants/{restaurantId}/users")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class UserController {
    UserService userService;

    @PostMapping
    public ApiResult<UserResponse> createUser(@PathVariable String restaurantId, @Valid @RequestBody CreateUserRequest request) {
        return ApiResult.<UserResponse>builder()
                .data(userService.createUser(restaurantId, request, SecurityUtils.getCurrentUserId()))
                .message("User created successfully")
                .build();
    }

    @GetMapping
    public ApiResult<List<UserResponse>> getUsers(@PathVariable String restaurantId) {
        return ApiResult.<List<UserResponse>>builder()
                .data(userService.getUsers(restaurantId, SecurityUtils.getCurrentUserId()))
                .build();
    }

    @DeleteMapping("/{userId}")
    public ApiResult<Void> deleteUser(@PathVariable String restaurantId, @PathVariable String userId) {
        userService.deleteUser(restaurantId, userId, SecurityUtils.getCurrentUserId());
        return ApiResult.<Void>builder()
                .message("User deleted successfully")
                .build();
    }
}
