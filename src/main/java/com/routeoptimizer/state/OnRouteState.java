package com.routeoptimizer.state;

import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.model.enums.OrderStatus;

/**
 * State for orders that are ON_ROUTE (in transit with a driver).
 * Allowed transitions:
 *   ON_ROUTE → DELIVERED (successful delivery)
 *   ON_ROUTE → RETURNED (failed delivery attempt, package returned)
 *   ON_ROUTE → CANCELLED (order cancelled during transit)
 */
public class OnRouteState extends AbstractOrderState {
    @Override
    public void transitionTo(Order order, OrderStatus newStatus, String reason) {
        switch (newStatus) {
            case DELIVERED -> {
                order.setActualDeliveryTime(java.time.LocalDateTime.now());
                order.setNonDeliveryReason(null);
                order.changeState(new DeliveredState());
            }
            case RETURNED -> {
                order.setActualDeliveryTime(java.time.LocalDateTime.now());
                order.setNonDeliveryReason(reason);
                order.changeState(new ReturnedState());
            }
            case CANCELLED -> {
                order.setActualDeliveryTime(java.time.LocalDateTime.now());
                order.setNonDeliveryReason(reason);
                order.changeState(new CancelledState());
            }
            default -> super.transitionTo(order, newStatus, reason);
        }
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.ON_ROUTE;
    }
}

