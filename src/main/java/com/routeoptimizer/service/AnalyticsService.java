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

    public AnalyticsService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    /**
     * Calcula las entregas exitosas comparando diferentes periodos de tiempo.
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
