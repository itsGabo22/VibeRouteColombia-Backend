package com.routeoptimizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.routeoptimizer.model.entity.Route;

import java.util.Optional;
import java.util.List;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
  @org.springframework.data.jpa.repository.Query("SELECT r FROM Route r LEFT JOIN FETCH r.stops WHERE r.batchId = :batchId")
  Optional<Route> findByBatchId(Long batchId);

  @org.springframework.data.jpa.repository.Query("SELECT r FROM Route r LEFT JOIN FETCH r.stops WHERE r.driverId = :driverId")
  Optional<Route> findByDriverId(Long driverId);

  @org.springframework.data.jpa.repository.Query("SELECT DISTINCT r FROM Route r LEFT JOIN FETCH r.stops")
  List<Route> findAllOptimized();
}
