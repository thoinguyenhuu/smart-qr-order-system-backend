package com.ht.project.smart_qr_menu_order_system.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/**
 * Global response wrapper used by every controller.
 */
@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResult<T> {

    @Builder.Default
    int success = 1;

    T data;

    String message;
}
