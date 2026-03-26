package com.ht.project.smart_qr_menu_order_system.dtos.response;

import lombok.Builder;

@Builder
public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
