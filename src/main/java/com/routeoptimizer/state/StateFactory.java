package com.routeoptimizer.state;

import com.routeoptimizer.model.enums.OrderStatus;

public class StateFactory {
    public static OrderState getState(OrderStatus status) {
        if (status == null) {
            return new PendingState();
        }
        return switch (status) {
            case PENDING -> new PendingState();
            case ON_ROUTE -> new OnRouteState();
            case DELIVERED -> new DeliveredState();
            case CANCELLED -> new CancelledState();
            case RETURNED -> new ReturnedState();
        };
    }
}
