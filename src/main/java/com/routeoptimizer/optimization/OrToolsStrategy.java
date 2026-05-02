package com.routeoptimizer.optimization;

import com.google.ortools.Loader;
import com.google.ortools.constraintsolver.Assignment;
import com.google.ortools.constraintsolver.FirstSolutionStrategy;
import com.google.ortools.constraintsolver.RoutingIndexManager;
import com.google.ortools.constraintsolver.RoutingModel;
import com.google.ortools.constraintsolver.RoutingSearchParameters;
import com.routeoptimizer.model.Coordinate;
import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.model.entity.Route;
import com.routeoptimizer.service.MapService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrToolsStrategy implements OptimizationStrategy {

  private static final Logger log = LoggerFactory.getLogger(OrToolsStrategy.class);
  private final MapService mapService;

  static {
    Loader.loadNativeLibraries();
  }

  public OrToolsStrategy(MapService mapService) {
    this.mapService = mapService;
  }

  @Override
  public Route optimize(List<Order> orders, Coordinate startLocation, String mode) {
    if (orders == null || orders.isEmpty()) {
      return new Route.Builder().build();
    }
    
    final String finalMode = (mode != null) ? mode : "EFFICIENCY";
    log.info("Iniciando optimización de ruta para {} pedidos en modo {}.", orders.size(), finalMode);

    final int numOrders = orders.size();
    final int numNodes = numOrders + 1;
    final int numVehicles = 1;
    final int depotNode = 0;

    RoutingIndexManager manager = new RoutingIndexManager(numNodes, numVehicles, depotNode);

    // MODO EFICIENCIA: Distancia Pura
    // MODO PRIORIDAD: Distancia con bonos
    boolean isPriorityMode = "PRIORITY".equalsIgnoreCase(finalMode);
    long[][] distanceMatrix = generateDistanceMatrix(orders, startLocation);

    RoutingModel routing = new RoutingModel(manager);

    final int transitCallbackIndex = routing.registerTransitCallback((long fromIndex, long toIndex) -> {
      int fromNode = manager.indexToNode(fromIndex);
      int toNode = manager.indexToNode(toIndex);
      if (fromNode >= distanceMatrix.length || toNode >= distanceMatrix[0].length) return 999999L;
      
      long distance = distanceMatrix[fromNode][toNode];
      
      if (toNode > 0 && isPriorityMode) {
        Order order = orders.get(toNode - 1);
        if (com.routeoptimizer.model.enums.Priority.HIGH.equals(order.getPriority())) {
            return (long)(distance * 0.4); // Fuerte atracción para HIGH
        } else if (com.routeoptimizer.model.enums.Priority.LOW.equals(order.getPriority())) {
            return (long)(distance * 1.5); // Leve rechazo para LOW
        }
      }
      return distance;
    });

    routing.setArcCostEvaluatorOfAllVehicles(transitCallbackIndex);

    RoutingSearchParameters searchParameters = com.google.ortools.constraintsolver.main.defaultRoutingSearchParameters()
        .toBuilder()
        .setFirstSolutionStrategy(FirstSolutionStrategy.Value.SAVINGS) // Clarke-Wright Savings: Produce rutas mucho más limpias y lógicas visualmente.
        .setTimeLimit(com.google.protobuf.Duration.newBuilder().setSeconds(2).build())
        .build();

    Assignment solution = routing.solveWithParameters(searchParameters);

    if (solution == null) {
      log.error("❌ No se encontró una solución óptima.");
      return new Route.Builder().forBatch(orders.get(0).getBatchId()).withStops(orders).build();
    }

    List<Order> optimizedStops = new ArrayList<>();
    long index = routing.start(0);
    long totalDistance = 0;

    while (!routing.isEnd(index)) {
      int nodeIndex = manager.indexToNode(index);
      if (nodeIndex > 0) {
        optimizedStops.add(orders.get(nodeIndex - 1));
      }
      long nextIndex = solution.value(routing.nextVar(index));
      totalDistance += routing.getArcCostForVehicle(index, nextIndex, 0);
      index = nextIndex;
    }

    return new Route.Builder()
        .forBatch(orders.get(0).getBatchId())
        .withStops(optimizedStops)
        .withTotalDistance(totalDistance)
        .build();
  }

  private long[][] generateDistanceMatrix(List<Order> orders, Coordinate startLocation) {
    List<Coordinate> points = new ArrayList<>();
    points.add(startLocation);
    for (Order o : orders) {
      points.add(new Coordinate(o.getLat(), o.getLng()));
    }
    return mapService.getDistanceMatrix(points);
  }
}
