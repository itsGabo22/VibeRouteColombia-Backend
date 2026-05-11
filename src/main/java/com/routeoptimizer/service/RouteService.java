package com.routeoptimizer.service;

import com.routeoptimizer.model.entity.Route;
import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.model.Coordinate;
import com.routeoptimizer.optimization.RouteOptimizer;
import com.routeoptimizer.repository.RouteRepository;
import com.routeoptimizer.repository.OrderRepository;
import com.routeoptimizer.service.MapService;
import com.routeoptimizer.exception.ResourceNotFoundException;

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
    return optimizeAndSaveRoute(batchId, null, null, "EFFICIENCY");
  }

  @Transactional
  public Route optimizeAndSaveRoute(Long batchId, Double startLat, Double startLng, String mode) {
    log.info("Starting optimization and persistence process for batch #{}", batchId);

    Coordinate startLoc = (startLat != null && startLng != null) ? new Coordinate(startLat, startLng) : null;
    Route optimizedResult = routeOptimizer.optimizeBatch(batchId, startLoc, mode);
    
    // Fetch detailed directions (encoded polyline) for the sequence
    String encodedPolyline = null;
    List<Order> stops = optimizedResult.getStops();
    if (stops != null && stops.size() > 1) {
       Coordinate origin = stops.get(0).getLocation(); 
       Coordinate destination = stops.get(stops.size() - 1).getLocation();
       
       if (origin != null && destination != null) {
           List<Coordinate> waypoints = stops.size() > 2 
              ? stops.subList(1, stops.size() - 1).stream()
                  .map(Order::getLocation)
                  .filter(loc -> loc != null) // Avoid null waypoints
                  .toList()
              : null;
           
           try {
               encodedPolyline = mapService.getDirections(origin, destination, waypoints);
           } catch (Exception e) {
               log.warn("Failed to get directions polyline: {}", e.getMessage());
           }
       } else {
           log.warn("Cannot generate directions: origin or destination location is null.");
       }
    }

    Route routeToSave = new Route.Builder()
        .forBatch(batchId)
        .withStops(stops)
        .withTotalDistance(optimizedResult.getTotalDistanceMeters())
        .withEstimatedTime(optimizedResult.getTotalDistanceMeters() / 5) // Promedio ~18km/h en ciudad
        .withEncodedPolyline(encodedPolyline)
        .build();

    @SuppressWarnings("null")
    Route savedRoute = routeRepository.save(routeToSave);
    
    // Link orders to the saved route and preserve delivery sequence
    if (stops != null) {
        for (int i = 0; i < stops.size(); i++) {
            Order o = stops.get(i);
            o.setRouteId(savedRoute.getId());
            o.setDeliveryOrder(i + 1); // Persistir el orden secuencial
            orderRepository.save(o);
        }
    }

    log.info("Route successfully persisted for batch #{}. Route ID: {}. Polyline length: {}", 
        batchId, savedRoute.getId(), encodedPolyline != null ? encodedPolyline.length() : 0);

    return savedRoute;
  }

  @Transactional(readOnly = true)
  public List<Route> findAll() {
    return routeRepository.findAllOptimized();
  }

  @Transactional(readOnly = true)
  @SuppressWarnings("null")
  public Route findById(Long id) {
    return routeRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Route not found with ID: " + id));
  }

  @Transactional(readOnly = true)
  public Route findByBatchId(Long batchId) {
    return routeRepository.findByBatchId(batchId)
        .orElseThrow(() -> new ResourceNotFoundException("There is no route generated for batch: " + batchId));
  }

  @Transactional(readOnly = true)
  public Route findByDriverId(Long driverId) {
    return routeRepository.findByDriverId(driverId)
        .orElseThrow(() -> new ResourceNotFoundException("There is no route assigned for driver: " + driverId));
  }
}
