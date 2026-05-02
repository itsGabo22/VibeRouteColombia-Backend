package com.routeoptimizer.state;

import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.model.enums.OrderStatus;

public interface OrderState {
    void transitionTo(Order order, OrderStatus newStatus, String reason);
    OrderStatus getStatus();
}
