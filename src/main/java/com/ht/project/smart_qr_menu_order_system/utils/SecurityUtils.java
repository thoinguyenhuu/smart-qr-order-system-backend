package com.ht.project.smart_qr_menu_order_system.utils;

import com.ht.project.smart_qr_menu_order_system.config.security.AuthenticatedUser;
import com.ht.project.smart_qr_menu_order_system.enums.Role;
import com.ht.project.smart_qr_menu_order_system.exception.AppErrorCode;
import com.ht.project.smart_qr_menu_order_system.exception.AppException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Helper methods for reading authenticated principal from SecurityContext.
 */
public final class SecurityUtils {
    private SecurityUtils() {
    }

    public static AuthenticatedUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser)) {
            throw new AppException(AppErrorCode.UNAUTHENTICATED);
        }
        return authenticatedUser;
    }

    public static String getCurrentUserId() {
        return getCurrentUser().userId();
    }

    public static Role getCurrentUserRole() {
        return getCurrentUser().role();
    }
}
