package com.routeoptimizer.state;

import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.model.enums.OrderStatus;

public abstract class AbstractOrderState implements OrderState {
    @Override
    public void transitionTo(Order order, OrderStatus newStatus, String reason) {
        throw new IllegalStateException("No se puede transicionar del estado " + getStatus() + " al estado " + newStatus);
    }
}
