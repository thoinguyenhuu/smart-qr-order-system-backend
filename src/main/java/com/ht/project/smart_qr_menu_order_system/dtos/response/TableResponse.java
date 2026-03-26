package com.ht.project.smart_qr_menu_order_system.dtos.response;

/**
 * Table response for owner management and QR distribution.
 */
public record TableResponse(
        String id,
        String restaurantId,
        int tableNumber,
        String qrCode,
        boolean available
) {
}
