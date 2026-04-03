package com.routeoptimizer.service;

import com.routeoptimizer.model.entity.Route;
import com.routeoptimizer.optimization.RouteOptimizer;
import com.routeoptimizer.repository.RouteRepository;

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

  public RouteService(RouteRepository routeRepository, RouteOptimizer routeOptimizer) {
    this.routeRepository = routeRepository;
    this.routeOptimizer = routeOptimizer;
  }

  @Transactional
  public Route optimizeAndSaveRoute(Long batchId) {
    log.info("Starting optimization and persistence process for batch #{}", batchId);

    Route newRoute = routeOptimizer.optimizeBatch(batchId);

    @SuppressWarnings("null")
    Route savedRoute = routeRepository.save(newRoute);

    log.info("Route successfully persisted for batch #{}. Route ID: {}", batchId, savedRoute.getId());

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
