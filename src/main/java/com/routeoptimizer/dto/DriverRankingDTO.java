package com.routeoptimizer.dto;

public record DriverRankingDTO(
        String driverName,
        long successfulDeliveries,
        double effectivenessPercentage,
        String tag) {
}
