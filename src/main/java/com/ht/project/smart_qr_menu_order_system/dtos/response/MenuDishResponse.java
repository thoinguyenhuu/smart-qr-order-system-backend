package com.ht.project.smart_qr_menu_order_system.dtos.response;

import java.math.BigDecimal;

/**
 * Dish item used in menu response.
 */
public record MenuDishResponse(
        String id,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        boolean available,
        boolean soldOutToday
) {
}
