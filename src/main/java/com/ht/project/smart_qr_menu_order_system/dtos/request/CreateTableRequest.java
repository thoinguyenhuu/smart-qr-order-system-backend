package com.ht.project.smart_qr_menu_order_system.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Request for creating restaurant table metadata.
 */
public record CreateTableRequest(
        @Min(value = 1, message = "Table number must be greater than zero")
        int tableNumber,

        @NotNull(message = "Availability is required")
        Boolean available
) {
}
