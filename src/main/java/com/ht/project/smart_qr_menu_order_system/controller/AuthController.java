package com.ht.project.smart_qr_menu_order_system.controller;

import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateUserRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.LoginRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.RefreshTokenRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.LoginResponse;
import com.ht.project.smart_qr_menu_order_system.dtos.response.UserResponse;
import com.ht.project.smart_qr_menu_order_system.service.AuthService;
import com.ht.project.smart_qr_menu_order_system.utils.ApiResult;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthController {
    AuthService authService ;

    @PostMapping("/login")
    public ApiResult<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        return ApiResult.<LoginResponse>builder()
                .data(authService.login(request))
                .build();
    }

    @PostMapping("/register")
    public ApiResult<UserResponse> register(@Valid @RequestBody CreateUserRequest request){
        return ApiResult.<UserResponse>builder()
                .message("Please check your email to verify!!!")
                .data(authService.register(request))
                .build();
    }

    @PostMapping("/refresh")
    public ApiResult<LoginResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ApiResult.<LoginResponse>builder()
                .data(authService.refreshToken(request))
                .message("Token refreshed successfully")
                .build();
    }

    @PostMapping("/logout")
    public ApiResult<Void> logout() {
        authService.logout();
        return ApiResult.<Void>builder()
                .message("Logout successfully")
                .build();
    }

    @GetMapping("/verify")
    public ApiResult<UserResponse> verify(@RequestParam String token){
        return ApiResult.<UserResponse>builder()
                .data(authService.verify(token))
                .build() ;
    }
}
