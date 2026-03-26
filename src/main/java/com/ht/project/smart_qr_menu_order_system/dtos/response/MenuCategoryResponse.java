package com.ht.project.smart_qr_menu_order_system.dtos.response;

import java.util.List;

/**
 * Category block used in menu response.
 */
public record MenuCategoryResponse(
        String id,
        String name,
        int displayOrder,
        List<MenuDishResponse> dishes
) {
}
