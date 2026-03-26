package com.ht.project.smart_qr_menu_order_system.dtos.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/**
 * Public order creation request.
 */
public record CreateOrderRequest(
        @NotBlank(message = "Table id is required")
        String tableId,

        String note,

        @Valid
        @NotEmpty(message = "Order items are required")
        List<CreateOrderItemRequest> items
) {
}
