package com.ht.project.smart_qr_menu_order_system.config.security;

import com.ht.project.smart_qr_menu_order_system.enums.Role;

/**
 * Lightweight authenticated user object stored in SecurityContext.
 */
public record AuthenticatedUser(
        String userId,
        String email,
        Role role
) {
}
