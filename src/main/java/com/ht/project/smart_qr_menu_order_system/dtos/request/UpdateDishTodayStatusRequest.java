package com.ht.project.smart_qr_menu_order_system.dtos.request;

import jakarta.validation.constraints.NotNull;

/**
 * Toggle dish sold-out status for the current business day.
 */
public record UpdateDishTodayStatusRequest(
        @NotNull(message = "Sold-out status is required")
        Boolean soldOutToday
) {
}
