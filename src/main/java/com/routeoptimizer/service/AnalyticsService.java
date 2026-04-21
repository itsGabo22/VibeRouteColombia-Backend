package com.routeoptimizer.service;

import com.routeoptimizer.dto.DeliveryStatisticsDTO;
import com.routeoptimizer.dto.DriverRankingDTO;
import com.routeoptimizer.model.enums.OrderStatus;
import com.routeoptimizer.repository.OrderRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio encargado de generar las analíticas y estadísticas del sistema.
 */
@Service
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final com.routeoptimizer.repository.RouteRepository routeRepository;
    private final com.routeoptimizer.repository.DriverRepository driverRepository;

    public AnalyticsService(OrderRepository orderRepository,
                            com.routeoptimizer.repository.RouteRepository routeRepository,
                            com.routeoptimizer.repository.DriverRepository driverRepository) {
        this.orderRepository = orderRepository;
        this.routeRepository = routeRepository;
        this.driverRepository = driverRepository;
    }

    /**
     * Calculates financial metrics: Revenue, Costs, and Net Profit.
     */
    @Transactional(readOnly = true)
    public com.routeoptimizer.dto.FinancialAnalyticsDTO getFinancialAnalytics(String cityFilter) {
        java.math.BigDecimal revenue;
        if (cityFilter != null && !cityFilter.isEmpty()) {
            revenue = orderRepository.getRevenueByCity(cityFilter);
        } else {
            revenue = orderRepository.getTotalRevenue();
        }
        
        if (revenue == null) revenue = java.math.BigDecimal.ZERO;

        java.math.BigDecimal totalCosts = java.math.BigDecimal.ZERO;
        List<com.routeoptimizer.model.entity.Route> routes = routeRepository.findAll();

        for (com.routeoptimizer.model.entity.Route route : routes) {
            if (route.getDriverId() != null) {
                java.util.Optional<com.routeoptimizer.model.entity.Driver> driverOpt = driverRepository.findById(route.getDriverId());
                if (driverOpt.isPresent()) {
                    com.routeoptimizer.model.entity.Driver d = driverOpt.get();
                    // Si hay filtro de ciudad, solo sumamos costos de conductores de esa ciudad
                    if (cityFilter != null && !cityFilter.isEmpty() && !cityFilter.equalsIgnoreCase(d.getAssignedCity())) {
                        continue;
                    }
                    if (d.getCostPerHour() != null) {
                        double hours = route.getEstimatedTimeSeconds() / 3600.0;
                        java.math.BigDecimal routeCost = d.getCostPerHour().multiply(java.math.BigDecimal.valueOf(hours));
                        totalCosts = totalCosts.add(routeCost);
                    }
                }
            }
        }

        java.math.BigDecimal netProfit = revenue.subtract(totalCosts);
        double margin = revenue.compareTo(java.math.BigDecimal.ZERO) > 0
                ? netProfit.divide(revenue, 4, java.math.RoundingMode.HALF_UP).doubleValue() * 100
                : 0;

        Map<String, java.math.BigDecimal> byCity = new HashMap<>();
        for (String city : List.of("Bogotá", "Pasto", "Medellín")) {
            java.math.BigDecimal cityRev = orderRepository.getRevenueByCity(city);
            byCity.put(city, cityRev != null ? cityRev : java.math.BigDecimal.ZERO);
        }

        Map<String, java.math.BigDecimal> monthlyRevenue = new HashMap<>();
        List<Object[]> monthlyData = orderRepository.getMonthlyRevenueNative(cityFilter);
        for (Object[] row : monthlyData) {
            String month = (String) row[0];
            Object rawTotal = row[1];
            java.math.BigDecimal total = java.math.BigDecimal.ZERO;
            if (rawTotal instanceof java.math.BigDecimal) {
                total = (java.math.BigDecimal) rawTotal;
            } else if (rawTotal instanceof Number) {
                total = java.math.BigDecimal.valueOf(((Number) rawTotal).doubleValue());
            }
            monthlyRevenue.put(month.trim(), total);
        }

        return new com.routeoptimizer.dto.FinancialAnalyticsDTO(revenue, totalCosts, netProfit, byCity, monthlyRevenue, margin);
    }

    /**
     * Calculates successful deliveries comparing different time periods.
     */
    @Transactional(readOnly = true)
    public DeliveryStatisticsDTO getDeliveryStatistics() {
        LocalDateTime todayStart = LocalDateTime.now().with(LocalTime.MIN);
        LocalDateTime weekStart = LocalDateTime.now().minusWeeks(1);
        LocalDateTime monthStart = LocalDateTime.now().minusMonths(1);

        long today = orderRepository.countSuccessfulDeliveriesSince(todayStart);
        long week = orderRepository.countSuccessfulDeliveriesSince(weekStart);
        long month = orderRepository.countSuccessfulDeliveriesSince(monthStart);

        Map<String, Long> byCity = new HashMap<>();
        byCity.put("Pasto", orderRepository.countByStatusAndCity(OrderStatus.DELIVERED, "Pasto"));
        byCity.put("Bogotá", orderRepository.countByStatusAndCity(OrderStatus.DELIVERED, "Bogotá"));
        byCity.put("Medellín", orderRepository.countByStatusAndCity(OrderStatus.DELIVERED, "Medellín"));

        return new DeliveryStatisticsDTO(today, week, month, byCity);
    }

    /**
     * Obtiene el ranking de los 5 repartidores más efectivos.
     */
    @Transactional(readOnly = true)
    public List<DriverRankingDTO> getEfficiencyRanking() {
        List<DriverRankingDTO> original = orderRepository.getDriverRankings(PageRequest.of(0, 5));

        // Post-procesar para asignar tags/condecoraciones según posición
        java.util.List<DriverRankingDTO> ranked = new java.util.ArrayList<>();
        for (int i = 0; i < original.size(); i++) {
            DriverRankingDTO dto = original.get(i);
            String tag = (i == 0) ? "🥇 Top Driver"
                    : (i == 1) ? "🥈 Excellent" : (i == 2) ? "🥉 Very Good" : "Efficient";

            ranked.add(new DriverRankingDTO(
                    dto.driverName(),
                    dto.successfulDeliveries(),
                    dto.effectivenessPercentage(),
                    tag));
        }
        return ranked;
    }
}
