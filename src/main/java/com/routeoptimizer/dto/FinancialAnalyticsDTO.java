package com.routeoptimizer.dto;
 
import java.math.BigDecimal;
import java.util.Map;
 
public record FinancialAnalyticsDTO(
    BigDecimal totalRevenue,
    BigDecimal operationalCosts,
    BigDecimal netProfit,
    Map<String, BigDecimal> revenueByCity,
    double profitMarginPercentage
) {}
