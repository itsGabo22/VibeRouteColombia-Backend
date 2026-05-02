package com.routeoptimizer.state;

import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.model.enums.OrderStatus;

public class PendingState extends AbstractOrderState {
    @Override
    public void transitionTo(Order order, OrderStatus newStatus, String reason) {
        if (newStatus == OrderStatus.ON_ROUTE) {
            order.setNonDeliveryReason(null);
            order.changeState(new OnRouteState());
        } else if (newStatus == OrderStatus.CANCELLED) {
            order.setNonDeliveryReason(reason);
            order.changeState(new CancelledState());
        } else {
            super.transitionTo(order, newStatus, reason);
        }
    }

    @Override
    public OrderStatus getStatus() {
        return OrderStatus.PENDING;
    }
}
