package com.ht.project.smart_qr_menu_order_system.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Single order item payload from customer.
 */
public record CreateOrderItemRequest(
        @NotBlank(message = "Dish id is required")
        String dishId,

        @Min(value = 1, message = "Quantity must be greater than zero")
        int quantity
) {
}
