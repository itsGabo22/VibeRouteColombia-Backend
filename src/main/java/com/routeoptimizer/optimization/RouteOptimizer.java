package com.routeoptimizer.optimization;

import com.routeoptimizer.model.Coordinate;
import com.routeoptimizer.model.entity.Batch;
import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.model.entity.Route;
import com.routeoptimizer.repository.BatchRepository;
import com.routeoptimizer.repository.OrderRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio que orquestra la optimización de un lote.
 * Coordina la obtención de datos, el cálculo del algoritmo y la persistencia de
 * los resultados.
 */
@Service
public class RouteOptimizer {

  private static final Logger log = LoggerFactory.getLogger(RouteOptimizer.class);

  private final OptimizationStrategy optimizationStrategy;
  private final BatchRepository batchRepository;
  private final OrderRepository orderRepository;

  public RouteOptimizer(OptimizationStrategy optimizationStrategy,
      BatchRepository batchRepository,
      OrderRepository orderRepository) {
    this.optimizationStrategy = optimizationStrategy;
    this.batchRepository = batchRepository;
    this.orderRepository = orderRepository;
  }

  /**
   * Optimiza el orden de entrega de los pedidos dentro de un lote.
   * 
   * @param loteId ID del lote a optimizar.
   * @return El lote con sus pedidos en el orden optimizado.
   */
  @Transactional
  @SuppressWarnings("null")
  public Route optimizeBatch(Long batchId, Coordinate startLocationOverride, String mode) {
    log.info("Optimizando ruta para el lote #{}", batchId);

    Batch batch = batchRepository.findById(batchId)
        .orElseThrow(() -> new RuntimeException("Lote no encontrado: " + batchId));

    List<Order> orders = batch.getOrders();
    if (orders == null || orders.isEmpty()) {
      log.warn("El lote #{} no tiene pedidos para optimizar.", batchId);
      return new Route.Builder().forBatch(batchId).build();
    }

    // Punto de partida: Prioridad al GPS en vivo, luego al guardado, luego al almacén
    Coordinate startLocation;
    if (startLocationOverride != null) {
      startLocation = startLocationOverride;
    } else if (batch.getDriver() != null && batch.getDriver().getLocation() != null) {
      startLocation = batch.getDriver().getLocation();
    } else {
      startLocation = new Coordinate(4.6097, -74.0817);
    }

    // 1. Ejecutar el motor de optimización (Estrategia)
    Route optimizedRoute = optimizationStrategy.optimize(orders, startLocation, mode);

    // 2. Persistir el orden (secuencia) en la base de datos
    List<Order> stops = optimizedRoute.getStops();
    for (int i = 0; i < stops.size(); i++) {
      Order o = stops.get(i);
      o.setDeliveryOrder(i + 1);
      orderRepository.save(o);
    }

    log.info("Lote #{} optimizado exitosamente. Distancia total estimada: {} m",
        batchId, optimizedRoute.getTotalDistanceMeters());

    return optimizedRoute;
  }
}
