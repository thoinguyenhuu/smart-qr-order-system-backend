package com.ht.project.smart_qr_menu_order_system.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CreateRestaurantRequest(
        @NotBlank(message = "Restaurant name is required")
        String name,
        @NotBlank(message = "Restaurant address is required")
        String address
) {
}
