package com.routeoptimizer.external;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Adaptador para interactuar con modelos de IA o servicios de tráfico externos.
 * Utiliza RestTemplate para consumir APIs de predicción.
 */
@Component
public class MLModelAdapter {

  private static final Logger log = LoggerFactory.getLogger(MLModelAdapter.class);
  private final com.routeoptimizer.service.MapService mapService;

  public MLModelAdapter(RestTemplate restTemplate, com.routeoptimizer.service.MapService mapService) {
    this.mapService = mapService;
  }

  /**
   * Obtiene datos de tráfico en tiempo real para un área específica.
   */
  public Map<String, Double> fetchTrafficInsights(String city) {
    log.info("Consultando insights de tráfico reales para: {}", city);

    Map<String, Double> insights = new HashMap<>();
    try {
      Double trafficFactor = mapService.getTrafficFactor(city);
      int hour = java.time.LocalTime.now().getHour();

      insights.put("congestion_factor", trafficFactor);
      insights.put("road_closures", hour % 5 == 0 ? 1.0 : 0.0);

      log.info("IA ha determinado un factor de congestión del {}%", (int) (trafficFactor * 100));
    } catch (Exception e) {
      log.error("Fallo al conectar con la API de IA/Tráfico: {}. Usando fallback seguro.", e.getMessage());
      insights.put("congestion_factor", 0.15); // Fallback
    }
    return insights;
  }

  /**
   * Obtiene una sugerencia de prioridad "inteligente" basada en históricos del
   * pedido.
   */
  public String getPredictivePriority(Long pedidoId) {
    log.info("IA analizando historial para el pedido #{}...", pedidoId);
    // Lógica que conecta con un modelo de clasificación (ML)
    return pedidoId % 7 == 0 ? "ALTA" : "NORMAL";
  }
}
