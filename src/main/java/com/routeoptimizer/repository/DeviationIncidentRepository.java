package com.routeoptimizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.routeoptimizer.model.entity.DeviationIncident;

import java.util.Optional;

@Repository
public interface DeviationIncidentRepository extends JpaRepository<DeviationIncident, Long> {
    Optional<DeviationIncident> findByDriverIdAndStatus(Long driverId, String status);
}
