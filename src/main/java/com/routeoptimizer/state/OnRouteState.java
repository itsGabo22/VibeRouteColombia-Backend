package com.routeoptimizer.state;

import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.model.enums.OrderStatus;

public class OnRouteState extends AbstractOrderState {
    @Override
    public void transitionTo(Order order, OrderStatus newStatus, String reason) {
        if (newStatus == OrderStatus.DELIVERED) {
            order.setNonDeliveryReason(null);
            order.changeState(new DeliveredState());
        } else if (newStatus == OrderStatus.RETURNED) {
            order.setNonDeliveryReason(reason);
            order.changeState(new ReturnedState());
        } else if (newStatus == OrderStatus.CANCELLED) {
            order.setNonDeliveryReason(reason);
            order.changeState(new CancelledState());
        } else {
            super.transitionTo(order, newStatus, reason);
        }
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.ON_ROUTE;
    }
}
