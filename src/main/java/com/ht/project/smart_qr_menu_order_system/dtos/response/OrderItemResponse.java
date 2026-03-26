package com.ht.project.smart_qr_menu_order_system.dtos.response;

import java.math.BigDecimal;

/**
 * Order item response returned to both customer and owner.
 */
public record OrderItemResponse(
        String id,
        String dishId,
        String dishName,
        int quantity,
        BigDecimal priceAtOrder,
        BigDecimal lineTotal
) {
}
