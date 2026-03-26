package com.ht.project.smart_qr_menu_order_system.dtos.response;

import com.ht.project.smart_qr_menu_order_system.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Order aggregate response for REST and WebSocket payload.
 */
public record OrderResponse(
        String id,
        String restaurantId,
        OrderStatus status,
        String note,
        Instant createdAt,
        int tableNumber,
        BigDecimal totalPrice,
        List<OrderItemResponse> items
) {
}
