package com.routeoptimizer.state;

import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.model.enums.OrderStatus;

public class CancelledState extends AbstractOrderState {
    @Override
    public OrderStatus getStatus() {
        return OrderStatus.CANCELLED;
    }
}
