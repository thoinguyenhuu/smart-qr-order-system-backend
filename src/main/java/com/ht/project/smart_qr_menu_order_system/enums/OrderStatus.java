package com.ht.project.smart_qr_menu_order_system.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * Simple order state machine for restaurant workflow.
 */
public enum OrderStatus {
    NEW,
    PROCESSING,
    COMPLETED,
    PAYMENT_REQUESTED,
    PAID,
    CANCELLED;

    public boolean canTransitionTo(OrderStatus targetStatus) {
        return allowedTransitions().contains(targetStatus);
    }

    public Set<OrderStatus> allowedTransitions() {
        return switch (this) {
            case NEW -> EnumSet.of(PROCESSING, CANCELLED);
            case PROCESSING -> EnumSet.of(COMPLETED, CANCELLED);
            case COMPLETED -> EnumSet.noneOf(OrderStatus.class);
            case PAYMENT_REQUESTED -> EnumSet.of(PAID);
            case PAID, CANCELLED -> EnumSet.noneOf(OrderStatus.class);
        };
    }
}
