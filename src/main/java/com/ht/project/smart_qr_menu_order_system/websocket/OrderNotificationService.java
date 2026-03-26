package com.ht.project.smart_qr_menu_order_system.websocket;

import com.ht.project.smart_qr_menu_order_system.dtos.response.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Publish real-time order messages to restaurant-specific STOMP topic.
 */
@Service
@RequiredArgsConstructor
public class OrderNotificationService {
    private final SimpMessagingTemplate simpMessagingTemplate;

    public void notifyRestaurant(String restaurantId, OrderResponse orderResponse) {
        simpMessagingTemplate.convertAndSend("/topic/orders/" + restaurantId, orderResponse);
    }
}
