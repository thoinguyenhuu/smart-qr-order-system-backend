package com.ht.project.smart_qr_menu_order_system.dtos.response;


public record UserResponse(
        String id,
        String fullName,
        String phone,
        String email,
        String role,
        boolean active
) {
}
