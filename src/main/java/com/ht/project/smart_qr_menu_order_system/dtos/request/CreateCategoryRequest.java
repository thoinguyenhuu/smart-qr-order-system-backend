package com.ht.project.smart_qr_menu_order_system.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * Request for creating a menu category.
 */
public record CreateCategoryRequest(
        @NotBlank(message = "Category name is required")
        String name,

        @Min(value = 0, message = "Display order must be greater than or equal to zero")
        int displayOrder
) {
}
