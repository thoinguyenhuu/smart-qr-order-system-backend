package com.ht.project.smart_qr_menu_order_system.dtos.response;

/**
 * Category management response.
 */
public record CategoryResponse(
        String id,
        String restaurantId,
        String name,
        int displayOrder
) {
}
