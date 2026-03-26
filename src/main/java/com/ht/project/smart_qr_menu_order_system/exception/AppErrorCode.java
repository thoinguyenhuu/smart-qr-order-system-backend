package com.ht.project.smart_qr_menu_order_system.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Central error catalog for business and security flow.
 */
@Getter
public enum AppErrorCode {
    USER_NOT_FOUND("USER_NOT_FOUND", HttpStatus.NOT_FOUND, "User not found"),
    RESTAURANT_NOT_FOUND("RESTAURANT_NOT_FOUND", HttpStatus.NOT_FOUND, "Restaurant not found"),
    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", HttpStatus.NOT_FOUND, "Category not found"),
    DISH_NOT_FOUND("DISH_NOT_FOUND", HttpStatus.NOT_FOUND, "Dish not found"),
    TABLE_NOT_FOUND("TABLE_NOT_FOUND", HttpStatus.NOT_FOUND, "Table not found"),
    ORDER_NOT_FOUND("ORDER_NOT_FOUND", HttpStatus.NOT_FOUND, "Order not found"),
    INVALID_TENANT("INVALID_TENANT", HttpStatus.BAD_REQUEST, "Invalid tenant identifier"),
    TENANT_REQUIRED("TENANT_REQUIRED", HttpStatus.BAD_REQUEST, "Tenant identifier is required"),
    ACCESS_DENIED("ACCESS_DENIED", HttpStatus.FORBIDDEN, "You do not have permission to access this restaurant"),
    UNAUTHENTICATED("UNAUTHENTICATED", HttpStatus.UNAUTHORIZED, "Unauthenticated"),
    TOKEN_NOT_VERIFY("TOKEN_NOT_VERIFY", HttpStatus.UNAUTHORIZED, "Token is invalid or expired"),
    USER_NOT_ACTIVE("USER_NOT_ACTIVE", HttpStatus.FORBIDDEN, "User is not active"),
    INVALID_CREDENTIALS("INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED, "Invalid email or password"),
    INVALID_ORDER_STATUS("INVALID_ORDER_STATUS", HttpStatus.BAD_REQUEST, "Invalid order status transition"),
    BAD_REQUEST("BAD_REQUEST", HttpStatus.BAD_REQUEST, "Bad request"),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    AppErrorCode(String code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
