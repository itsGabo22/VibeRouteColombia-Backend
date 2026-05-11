package com.routeoptimizer.state;

import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.model.enums.OrderStatus;

/**
 * State for PENDING orders.
 * Allowed transitions:
 *   PENDING → ON_ROUTE (assigned to a driver)
 *   PENDING → DELIVERED (direct delivery without explicit routing)
 *   PENDING → CANCELLED (order cancelled before dispatch)
 *   PENDING → RETURNED (order returned before delivery attempt)
 */
public class PendingState extends AbstractOrderState {
    @Override
    public void transitionTo(Order order, OrderStatus newStatus, String reason) {
        switch (newStatus) {
            case ON_ROUTE -> {
                order.setNonDeliveryReason(null);
                order.changeState(new OnRouteState());
            }
            case DELIVERED -> {
                order.setActualDeliveryTime(java.time.LocalDateTime.now());
                order.setNonDeliveryReason(null);
                order.changeState(new DeliveredState());
            }
            case CANCELLED -> {
                order.setActualDeliveryTime(java.time.LocalDateTime.now());
                order.setNonDeliveryReason(reason);
                order.changeState(new CancelledState());
            }
            case RETURNED -> {
                order.setActualDeliveryTime(java.time.LocalDateTime.now());
                order.setNonDeliveryReason(reason);
                order.changeState(new ReturnedState());
            }
            default -> super.transitionTo(order, newStatus, reason);
        }
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PENDING;
    }
}

