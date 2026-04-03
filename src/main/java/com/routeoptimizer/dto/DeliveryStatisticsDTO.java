package com.routeoptimizer.dto;

import java.util.Map;

public record DeliveryStatisticsDTO(
        long today,
        long thisWeek,
        long thisMonth,
        Map<String, Long> deliveriesByCity) {
}
