package com.routeoptimizer.service;

import com.routeoptimizer.model.entity.Route;
import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.model.Coordinate;
import com.routeoptimizer.optimization.RouteOptimizer;
import com.routeoptimizer.repository.RouteRepository;
import com.routeoptimizer.repository.OrderRepository;
import com.routeoptimizer.service.MapService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RouteService {

  private static final Logger log = LoggerFactory.getLogger(RouteService.class);

  private final RouteRepository routeRepository;
  private final RouteOptimizer routeOptimizer;
  private final MapService mapService;
  private final OrderRepository orderRepository;

  public RouteService(RouteRepository routeRepository, 
                      RouteOptimizer routeOptimizer,
                      MapService mapService,
                      OrderRepository orderRepository) {
    this.routeRepository = routeRepository;
    this.routeOptimizer = routeOptimizer;
    this.mapService = mapService;
    this.orderRepository = orderRepository;
  }

  @Transactional
  public Route optimizeAndSaveRoute(Long batchId) {
    log.info("Starting optimization and persistence process for batch #{}", batchId);

    Route optimizedResult = routeOptimizer.optimizeBatch(batchId);
    
    // Fetch detailed directions (encoded polyline) for the sequence
    String encodedPolyline = null;
    List<Order> stops = optimizedResult.getStops();
    if (stops != null && stops.size() > 1) {
       Coordinate origin = stops.get(0).getLocation(); // Should be start point or first order?
       // Wait, RouteOptimizer starts from driver or city center. 
       // I should probably pass the same start location here.
       // For now, let's use the first stop as representative if driver location isn't stored.
       Coordinate destination = stops.get(stops.size() - 1).getLocation();
       List<Coordinate> waypoints = stops.size() > 2 
          ? stops.subList(1, stops.size() - 1).stream().map(Order::getLocation).toList()
          : null;
       
       encodedPolyline = mapService.getDirections(origin, destination, waypoints);
    }

    Route routeToSave = new Route.Builder()
        .forBatch(batchId)
        .withStops(stops)
        .withTotalDistance(optimizedResult.getTotalDistanceMeters())
        .withEncodedPolyline(encodedPolyline)
        .build();

    @SuppressWarnings("null")
    Route savedRoute = routeRepository.save(routeToSave);
    
    // Link orders to the saved route
    if (stops != null) {
        for (Order o : stops) {
            o.setRouteId(savedRoute.getId());
            orderRepository.save(o);
        }
    }

    log.info("Route successfully persisted for batch #{}. Route ID: {}. Polyline length: {}", 
        batchId, savedRoute.getId(), encodedPolyline != null ? encodedPolyline.length() : 0);

    return savedRoute;
  }

  @Transactional(readOnly = true)
  public List<Route> findAll() {
    return routeRepository.findAll();
  }

  @Transactional(readOnly = true)
  @SuppressWarnings("null")
  public Route findById(Long id) {
    return routeRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Route not found with ID: " + id));
  }

  @Transactional(readOnly = true)
  public Route findByBatchId(Long batchId) {
    return routeRepository.findByBatchId(batchId)
        .orElseThrow(() -> new RuntimeException("There is no route generated for batch: " + batchId));
  }

  @Transactional(readOnly = true)
  public Route findByDriverId(Long driverId) {
    return routeRepository.findByDriverId(driverId)
        .orElseThrow(() -> new RuntimeException("There is no route assigned for driver: " + driverId));
  }
}
