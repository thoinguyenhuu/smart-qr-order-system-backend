package com.ht.project.smart_qr_menu_order_system.dtos.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request for updating a dish.
 */
public record UpdateDishRequest(
        @NotBlank(message = "Dish name is required")
        String name,

        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
        BigDecimal price,

        String imageUrl,

        @NotNull(message = "Availability is required")
        Boolean available
) {
}
