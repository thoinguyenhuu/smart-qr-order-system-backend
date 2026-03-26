package com.ht.project.smart_qr_menu_order_system.service.impl;

import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateOrderItemRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.CreateOrderRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.request.UpdateOrderStatusRequest;
import com.ht.project.smart_qr_menu_order_system.dtos.response.OrderItemResponse;
import com.ht.project.smart_qr_menu_order_system.dtos.response.OrderResponse;
import com.ht.project.smart_qr_menu_order_system.entity.Dish;
import com.ht.project.smart_qr_menu_order_system.entity.Order;
import com.ht.project.smart_qr_menu_order_system.entity.OrderItem;
import com.ht.project.smart_qr_menu_order_system.entity.Restaurant;
import com.ht.project.smart_qr_menu_order_system.entity.Tables;
import com.ht.project.smart_qr_menu_order_system.enums.OrderStatus;
import com.ht.project.smart_qr_menu_order_system.exception.AppErrorCode;
import com.ht.project.smart_qr_menu_order_system.exception.AppException;
import com.ht.project.smart_qr_menu_order_system.repository.DishRepository;
import com.ht.project.smart_qr_menu_order_system.repository.OrderItemRepository;
import com.ht.project.smart_qr_menu_order_system.repository.OrderRepository;
import com.ht.project.smart_qr_menu_order_system.repository.RestaurantRepository;
import com.ht.project.smart_qr_menu_order_system.repository.RestaurantUserRepository;
import com.ht.project.smart_qr_menu_order_system.repository.TablesRepository;
import com.ht.project.smart_qr_menu_order_system.service.OrderService;
import com.ht.project.smart_qr_menu_order_system.websocket.OrderNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Order service contains creation flow, state transition and WebSocket notify.
 */
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private static final List<OrderStatus> CUSTOMER_ACTIVE_ORDER_STATUSES =
            List.of(
                    OrderStatus.NEW,
                    OrderStatus.PROCESSING,
                    OrderStatus.COMPLETED,
                    OrderStatus.PAYMENT_REQUESTED
            );
    private static final List<OrderStatus> PAYMENT_REQUEST_ELIGIBLE_STATUSES =
            List.of(
                    OrderStatus.NEW,
                    OrderStatus.PROCESSING,
                    OrderStatus.COMPLETED,
                    OrderStatus.PAYMENT_REQUESTED
            );
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final RestaurantRepository restaurantRepository;
    private final TablesRepository tablesRepository;
    private final DishRepository dishRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final RestaurantUserRepository restaurantUserRepository;
    private final OrderNotificationService orderNotificationService;

    @Override
    @Transactional
    public OrderResponse createOrder(String restaurantId, CreateOrderRequest request) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new AppException(AppErrorCode.RESTAURANT_NOT_FOUND));
        Tables table = tablesRepository.findByIdAndRestaurantId(request.tableId(), restaurantId)
                .orElseThrow(() -> new AppException(AppErrorCode.TABLE_NOT_FOUND));

        if (!Boolean.TRUE.equals(table.getAvailable())) {
            throw new AppException(AppErrorCode.BAD_REQUEST, "Table is not available");
        }

        Order order = Order.builder()
                .restaurant(restaurant)
                .status(OrderStatus.NEW)
                .note(request.note())
                .tableNumber(table.getTableNumber())
                .totalPrice(BigDecimal.ZERO)
                .build();
        order.setTenantId(restaurantId);
        Order savedOrder = orderRepository.save(order);

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        for (CreateOrderItemRequest itemRequest : request.items()) {
            Dish dish = dishRepository.findByIdAndCategoryRestaurantId(itemRequest.dishId(), restaurantId)
                    .orElseThrow(() -> new AppException(AppErrorCode.DISH_NOT_FOUND));
            if (!dish.isAvailableForOrder(BUSINESS_ZONE)) {
                throw new AppException(
                        AppErrorCode.BAD_REQUEST,
                        dish.isSoldOutToday(BUSINESS_ZONE) ? "Món này đã hết trong hôm nay" : "Món này đang tạm ngưng bán"
                );
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .dish(dish)
                    .quantity(itemRequest.quantity())
                    .priceAtOrder(dish.getPrice())
                    .build();
            orderItem.setTenantId(restaurantId);
            orderItems.add(orderItem);
            totalPrice = totalPrice.add(dish.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }

        orderItemRepository.saveAll(orderItems);
        savedOrder.setTotalPrice(totalPrice);
        savedOrder = orderRepository.save(savedOrder);

        OrderResponse orderResponse = mapOrder(savedOrder, orderItems);
        notifyViaWebSocket(orderResponse);
        return orderResponse;
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(String restaurantId, String orderId, UpdateOrderStatusRequest request, String userId) {
        validateRestaurantAccess(restaurantId, userId);
        Order order = orderRepository.findByIdAndRestaurantId(orderId, restaurantId)
                .orElseThrow(() -> new AppException(AppErrorCode.ORDER_NOT_FOUND));

        if (!order.getStatus().canTransitionTo(request.status())) {
            throw new AppException(AppErrorCode.INVALID_ORDER_STATUS,
                    "Cannot change status from " + order.getStatus() + " to " + request.status());
        }

        order.setStatus(request.status());
        Order savedOrder = orderRepository.save(order);
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderIdOrderByIdAsc(savedOrder.getId());
        OrderResponse orderResponse = mapOrder(savedOrder, orderItems);
        notifyViaWebSocket(orderResponse);
        return orderResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders(String restaurantId, String userId) {
        validateRestaurantAccess(restaurantId, userId);
        return orderRepository.findAllByRestaurantIdOrderByCreatedAtDesc(restaurantId)
                .stream()
                .map(order -> mapOrder(order, orderItemRepository.findAllByOrderIdOrderByIdAsc(order.getId())))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(String restaurantId, String orderId, String userId) {
        validateRestaurantAccess(restaurantId, userId);
        Order order = orderRepository.findByIdAndRestaurantId(orderId, restaurantId)
                .orElseThrow(() -> new AppException(AppErrorCode.ORDER_NOT_FOUND));
        return mapOrder(order, orderItemRepository.findAllByOrderIdOrderByIdAsc(orderId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getActiveOrdersByTable(String restaurantId, String tableId) {
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new AppException(AppErrorCode.RESTAURANT_NOT_FOUND));
        Tables table = tablesRepository.findByIdAndRestaurantId(tableId, restaurantId)
                .orElseThrow(() -> new AppException(AppErrorCode.TABLE_NOT_FOUND));

        return orderRepository.findAllByRestaurantIdAndTableNumberAndStatusInOrderByCreatedAtDesc(
                        restaurantId,
                        table.getTableNumber(),
                        CUSTOMER_ACTIVE_ORDER_STATUSES
                )
                .stream()
                .map(order -> mapOrder(order, orderItemRepository.findAllByOrderIdOrderByIdAsc(order.getId())))
                .toList();
    }

    @Override
    @Transactional
    public List<OrderResponse> requestPayment(String restaurantId, String tableId) {
        restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new AppException(AppErrorCode.RESTAURANT_NOT_FOUND));
        Tables table = tablesRepository.findByIdAndRestaurantId(tableId, restaurantId)
                .orElseThrow(() -> new AppException(AppErrorCode.TABLE_NOT_FOUND));

        return requestPaymentForTableNumber(restaurantId, table.getTableNumber());
    }

    @Override
    @Transactional
    public List<OrderResponse> requestPaymentByTableNumber(String restaurantId, int tableNumber, String userId) {
        validateRestaurantAccess(restaurantId, userId);
        tablesRepository.findByRestaurantIdAndTableNumber(restaurantId, tableNumber)
                .orElseThrow(() -> new AppException(AppErrorCode.TABLE_NOT_FOUND));
        return requestPaymentForTableNumber(restaurantId, tableNumber);
    }

    private List<OrderResponse> requestPaymentForTableNumber(String restaurantId, int tableNumber) {
        List<Order> activeOrders = orderRepository.findAllByRestaurantIdAndTableNumberAndStatusInOrderByCreatedAtDesc(
                restaurantId,
                tableNumber,
                PAYMENT_REQUEST_ELIGIBLE_STATUSES
        );

        if (activeOrders.isEmpty()) {
            throw new AppException(AppErrorCode.BAD_REQUEST, "Bàn này chưa có món để tính tiền");
        }

        boolean hasIncompleteOrders = activeOrders.stream()
                .anyMatch(order -> order.getStatus() == OrderStatus.NEW || order.getStatus() == OrderStatus.PROCESSING);
        if (hasIncompleteOrders) {
            throw new AppException(
                    AppErrorCode.BAD_REQUEST,
                    "Chỉ có thể yêu cầu tính tiền khi mọi món của bàn đã hoàn tất"
            );
        }

        List<Order> ordersToUpdate = activeOrders.stream()
                .filter(order -> order.getStatus() == OrderStatus.COMPLETED)
                .toList();
        Set<String> updatedOrderIds = ordersToUpdate.stream()
                .map(Order::getId)
                .collect(java.util.stream.Collectors.toSet());

        ordersToUpdate.forEach(order -> order.setStatus(OrderStatus.PAYMENT_REQUESTED));
        if (!ordersToUpdate.isEmpty()) {
            orderRepository.saveAll(ordersToUpdate);
        }

        return activeOrders.stream()
                .map(order -> {
                    OrderResponse orderResponse = mapOrder(
                            order,
                            orderItemRepository.findAllByOrderIdOrderByIdAsc(order.getId())
                    );
                    if (updatedOrderIds.contains(order.getId())) {
                        notifyViaWebSocket(orderResponse);
                    }
                    return orderResponse;
                })
                .toList();
    }

    @Override
    public void notifyViaWebSocket(OrderResponse orderResponse) {
        orderNotificationService.notifyRestaurant(orderResponse.restaurantId(), orderResponse);
    }

    private void validateRestaurantAccess(String restaurantId, String userId) {
        if (!restaurantUserRepository.existsByRestaurantIdAndUserId(restaurantId, userId)) {
            throw new AppException(AppErrorCode.ACCESS_DENIED);
        }
    }

    private OrderResponse mapOrder(Order order, List<OrderItem> orderItems) {
        List<OrderItemResponse> itemResponses = orderItems.stream()
                .map(orderItem -> new OrderItemResponse(
                        orderItem.getId(),
                        orderItem.getDish().getId(),
                        orderItem.getDish().getName(),
                        orderItem.getQuantity(),
                        orderItem.getPriceAtOrder(),
                        orderItem.getPriceAtOrder().multiply(BigDecimal.valueOf(orderItem.getQuantity()))
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getRestaurant().getId(),
                order.getStatus(),
                order.getNote(),
                order.getCreatedAt(),
                order.getTableNumber(),
                order.getTotalPrice(),
                itemResponses
        );
    }
}
