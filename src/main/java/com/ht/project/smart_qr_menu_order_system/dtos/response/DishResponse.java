package com.ht.project.smart_qr_menu_order_system.dtos.response;

import java.math.BigDecimal;

/**
 * Dish management response.
 */
public record DishResponse(
        String id,
        String restaurantId,
        String categoryId,
        String name,
        String description,
        BigDecimal price,
        String imageUrl,
        boolean available,
        boolean soldOutToday,
        boolean availableForOrder
) {
}
