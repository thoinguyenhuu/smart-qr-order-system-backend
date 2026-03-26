package com.ht.project.smart_qr_menu_order_system.dtos.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for refreshing JWT tokens.
 */
public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token is required")
        String refreshToken
) {
}
