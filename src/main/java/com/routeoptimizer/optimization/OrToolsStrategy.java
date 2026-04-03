package com.routeoptimizer.optimization;

import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.*;
import com.routeoptimizer.model.Coordinate;
import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.model.entity.Route;
import com.routeoptimizer.service.MapService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementación de la estrategia de optimización utilizando Google OR-Tools.
 * Resuelve el Problema del Viajante (TSP) para un lote de pedidos.
 */
@Component
public class OrToolsStrategy implements OptimizationStrategy {

  private static final Logger log = LoggerFactory.getLogger(OrToolsStrategy.class);
  private final MapService mapService;

  static {
    // Cargar librerías nativas de OR-Tools
    Loader.loadNativeLibraries();
  }

  public OrToolsStrategy(MapService mapService) {
    this.mapService = mapService;
  }

  @Override
  public Route optimize(List<Order> orders, Coordinate startLocation) {
    if (orders == null || orders.isEmpty()) {
      return new Route.Builder().build();
    }

    log.info("Iniciando optimización de ruta para {} pedidos.", orders.size());

    // 1. Preparar lista de puntos (Inicio + Destinos de los pedidos)
    List<Coordinate> puntos = new ArrayList<>();
    puntos.add(startLocation); // Nodo 0: Punto de partida (Almacén o Repartidor)
    for (Order p : orders) {
      puntos.add(new Coordinate(p.getLat(), p.getLng()));
    }

    // 2. Obtener matriz de distancias real de Google Maps
    long[][] distanceMatrix = mapService.getDistanceMatrix(puntos);

    // 3. Configurar el Manager de Índices (n nodos, 1 vehículo, 1 depósito en nodo
    // 0)
    RoutingIndexManager manager = new RoutingIndexManager(distanceMatrix.length, 1, 0);

    // 4. Crear el Modelo de Ruteo
    RoutingModel routing = new RoutingModel(manager);

    // 5. Crear la función de costo (Distancia)
    final int transitCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) -> {
      int fromNode = manager.indexToNode(fromIndex);
      int toNode = manager.indexToNode(toIndex);
      return distanceMatrix[fromNode][toNode];
    });

    // 6. Definir el costo del arco
    routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

    // 7. Parámetros de búsqueda
    RoutingSearchParameters searchParameters = com.google.ortools.constraintsolver.main.defaultRoutingSearchParameters()
        .toBuilder()
        .setFirstSolutionStrategy(FirstSolutionStrategy.Value.PATH_CHEAPEST_ARC)
        .build();

    // 8. Resolver
    Assignment solution = routing.solveWithParameters(searchParameters);

    if (solution == null) {
      log.error("❌ No se encontró una solución óptima. Retornando orden original.");
      return new Route.Builder()
          .forBatch(orders.get(0).getBatchId())
          .withStops(orders)
          .build();
    }

    // 9. Reconstruir la lista de pedidos en el orden óptimo
    List<Order> pedidosOrdenados = new ArrayList<>();
    long index = routing.start(0);
    index = solution.value(routing.nextVar(index)); // El primer paso es del nodo 0 a un pedido

    while (!routing.isEnd(index)) {
      int nodeIndex = manager.indexToNode(index);
      pedidosOrdenados.add(orders.get(nodeIndex - 1));
      index = solution.value(routing.nextVar(index));
    }

    long distanciaTotalMetros = solution.objectiveValue();
    log.info("✅ Optimización completada. Ruta de {} m calculada.", distanciaTotalMetros);

    // 10. Retornar objeto Ruta usando el Builder (Patrón de Software)
    return new Route.Builder()
        .forBatch(orders.get(0).getBatchId())
        .withStops(pedidosOrdenados)
        .withTotalDistance(distanciaTotalMetros)
        .build();
  }
}
