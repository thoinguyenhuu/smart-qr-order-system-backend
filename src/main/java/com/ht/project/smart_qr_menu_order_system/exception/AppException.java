package com.ht.project.smart_qr_menu_order_system.exception;

import lombok.Getter;

/**
 * Custom runtime exception used by services and filters.
 */
@Getter
public class AppException extends RuntimeException {
    private final AppErrorCode errorCode;
    private final String handleMessage;

    public AppException(AppErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.handleMessage = errorCode.getMessage();
    }

    public AppException(AppErrorCode errorCode, String handleMessage) {
        super(handleMessage);
        this.errorCode = errorCode;
        this.handleMessage = handleMessage;
    }
}
