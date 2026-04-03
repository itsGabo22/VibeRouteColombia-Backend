package com.routeoptimizer.service;

import com.routeoptimizer.external.MLModelAdapter;
import com.routeoptimizer.model.entity.Batch;
import com.routeoptimizer.model.entity.Order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio que proporciona sugerencias inteligentes para la optimización de
 * rutas integrando datos del motor de IA (tráfico real), el contexto de negocio
 * y Google Gemini como asesor contextual.
 */
@Service
public class AIRouteSuggestionService {

  private static final Logger log = LoggerFactory.getLogger(AIRouteSuggestionService.class);
  private final MLModelAdapter mlModelAdapter;
  private final ContextualAdvisor contextualAdvisor;

  public AIRouteSuggestionService(MLModelAdapter mlModelAdapter, ContextualAdvisor contextualAdvisor) {
    this.mlModelAdapter = mlModelAdapter;
    this.contextualAdvisor = contextualAdvisor;
  }

  /**
   * Analiza un lote y proporciona sugerencias antes de ejecutar la optimización
   * de OR-Tools. Ahora incluye recomendaciones en lenguaje natural de Gemini.
   */
  public Map<String, Object> sugerirOptimizacion(Batch batch) {
    log.info("Generando sugerencias inteligentes para el lote: {}", batch.getId());

    List<Order> orders = batch.getOrders();

    String city = orders.stream()
        .filter(p -> p.getCity() != null && !p.getCity().isBlank())
        .map(Order::getCity)
        .findFirst()
        .orElse("Bogotá");

    Map<String, Double> trafficData = mlModelAdapter.fetchTrafficInsights(city);

    long urgentOrders = orders.stream()
        .filter(p -> "HIGH".equals(mlModelAdapter.getPredictivePriority(p.getId())))
        .count();

    String aiRecommendation = contextualAdvisor.generatePreRouteRecommendation(
        batch, trafficData, urgentOrders);

    Map<String, Object> suggestion = new HashMap<>();
    suggestion.put("batchId", batch.getId());
    suggestion.put("city", city);
    suggestion.put("totalOrders", orders.size());
    suggestion.put("trafficInsights", trafficData);
    suggestion.put("urgentOrdersCount", urgentOrders);
    suggestion.put("aiRecommendation", aiRecommendation);
    suggestion.put("runOptimized", true);

    log.info("Sugerencia de IA para el lote #{}: {}", batch.getId(), aiRecommendation);

    return suggestion;
  }
}
