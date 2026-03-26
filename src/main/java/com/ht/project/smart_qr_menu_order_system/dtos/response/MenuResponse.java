package com.ht.project.smart_qr_menu_order_system.dtos.response;

import java.util.List;

/**
 * Public menu response by restaurant UUID.
 */
public record MenuResponse(
        String restaurantId,
        String restaurantName,
        List<MenuCategoryResponse> categories
) {
}
