package com.ht.project.smart_qr_menu_order_system.dtos.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for updating restaurant information.
 */
public record UpdateRestaurantRequest(
        @NotBlank(message = "Restaurant name is required")
        String name,

        @NotBlank(message = "Restaurant address is required")
        String address
) {
}
