package com.ht.project.smart_qr_menu_order_system.service;

import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateUserRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.LoginRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.RefreshTokenRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.LoginResponse;
import com.ht.project.smart_qr_menu_order_system.dtos.response.UserResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);

    void logout();

    LoginResponse refreshToken(RefreshTokenRequest request);

    boolean authenticate(String token);

    UserResponse register(CreateUserRequest request);

    UserResponse verify(String token);
}
