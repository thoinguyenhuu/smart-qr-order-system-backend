package com.ht.project.smart_qr_menu_order_system.service;

import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateOrderRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.UpdateOrderStatusRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.OrderResponse;

import java.util.List;

public interface OrderService {
    OrderResponse createOrder(String restaurantId, CreateOrderRequest request);

    OrderResponse updateOrderStatus(String restaurantId, String orderId, UpdateOrderStatusRequest request, String userId);

    List<OrderResponse> getOrders(String restaurantId, String userId);

    OrderResponse getOrder(String restaurantId, String orderId, String userId);

    List<OrderResponse> getActiveOrdersByTable(String restaurantId, String tableId);

    List<OrderResponse> requestPayment(String restaurantId, String tableId);

    List<OrderResponse> requestPaymentByTableNumber(String restaurantId, int tableNumber, String userId);

    void notifyViaWebSocket(OrderResponse orderResponse);
}
