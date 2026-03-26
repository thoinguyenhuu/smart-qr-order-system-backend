package com.ht.project.smart_qr_menu_order_system.controller;

import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateOrderRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.UpdateOrderStatusRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.OrderResponse;
import com.ht.project.smart_qr_menu_order_system.service.OrderService;
import com.ht.project.smart_qr_menu_order_system.utils.ApiResult;
import com.ht.project.smart_qr_menu_order_system.utils.SecurityUtils;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Public and protected order APIs using restaurant UUID as tenant key.
 */
@RestController
@RequestMapping("/restaurants/{restaurantId}/orders")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class OrderController {
    OrderService orderService;

    @PostMapping
    ApiResult<OrderResponse> createOrder(@PathVariable String restaurantId, @Valid @RequestBody CreateOrderRequest request) {
        return ApiResult.<OrderResponse>builder()
                .data(orderService.createOrder(restaurantId, request))
                .message("Order created successfully")
                .build();
    }

    @GetMapping
    ApiResult<List<OrderResponse>> getOrders(@PathVariable String restaurantId) {
        return ApiResult.<List<OrderResponse>>builder()
                .data(orderService.getOrders(restaurantId, SecurityUtils.getCurrentUserId()))
                .build();
    }

    @GetMapping("/{orderId}")
    ApiResult<OrderResponse> getOrder(@PathVariable String restaurantId, @PathVariable String orderId) {
        return ApiResult.<OrderResponse>builder()
                .data(orderService.getOrder(restaurantId, orderId, SecurityUtils.getCurrentUserId()))
                .build();
    }

    @GetMapping("/tables/{tableId}/active")
    ApiResult<List<OrderResponse>> getActiveOrdersByTable(@PathVariable String restaurantId, @PathVariable String tableId) {
        return ApiResult.<List<OrderResponse>>builder()
                .data(orderService.getActiveOrdersByTable(restaurantId, tableId))
                .build();
    }

    @PostMapping("/tables/{tableId}/request-payment")
    ApiResult<List<OrderResponse>> requestPayment(@PathVariable String restaurantId, @PathVariable String tableId) {
        return ApiResult.<List<OrderResponse>>builder()
                .data(orderService.requestPayment(restaurantId, tableId))
                .message("Payment requested successfully")
                .build();
    }

    @PostMapping("/tables/numbers/{tableNumber}/request-payment")
    ApiResult<List<OrderResponse>> requestPaymentByTableNumber(@PathVariable String restaurantId,
                                                               @PathVariable int tableNumber) {
        return ApiResult.<List<OrderResponse>>builder()
                .data(orderService.requestPaymentByTableNumber(
                        restaurantId,
                        tableNumber,
                        SecurityUtils.getCurrentUserId()
                ))
                .message("Payment requested successfully")
                .build();
    }

    @PatchMapping("/{orderId}/status")
    ApiResult<OrderResponse> updateOrderStatus(@PathVariable String restaurantId,
                                               @PathVariable String orderId,
                                               @Valid @RequestBody UpdateOrderStatusRequest request) {
        return ApiResult.<OrderResponse>builder()
                .data(orderService.updateOrderStatus(restaurantId, orderId, request, SecurityUtils.getCurrentUserId()))
                .message("Order status updated successfully")
                .build();
    }
}
