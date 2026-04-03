package com.routeoptimizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.routeoptimizer.model.entity.Route;

import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
  Optional<Route> findByBatchId(Long batchId);

  Optional<Route> findByDriverId(Long driverId);
}
