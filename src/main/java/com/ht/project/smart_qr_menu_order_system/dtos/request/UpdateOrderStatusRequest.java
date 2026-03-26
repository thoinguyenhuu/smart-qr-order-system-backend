package com.ht.project.smart_qr_menu_order_system.dtos.request;

import com.ht.project.smart_qr_menu_order_system.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Owner/cashier request for changing order status.
 */
public record UpdateOrderStatusRequest(
        @NotNull(message = "Status is required")
        OrderStatus status
) {
}
