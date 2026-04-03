package com.routeoptimizer.service;

import com.routeoptimizer.model.Coordinate;
import com.routeoptimizer.model.entity.DeviationIncident;
import com.routeoptimizer.repository.DeviationIncidentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DeviationDetector {

  private static final Logger log = LoggerFactory.getLogger(DeviationDetector.class);
  private static final double STOP_ZONE_RADIUS_METERS = 100.0;
  private static final double DEVIATION_THRESHOLD_METERS = 200.0;
  private static final double EARTH_RADIUS_METERS = 6_371_000.0;

  private final ContextualAdvisor contextualAdvisor;
  private final BatchService batchService;
  private final DeviationIncidentRepository incidentRepository;

  public DeviationDetector(ContextualAdvisor contextualAdvisor,
      BatchService batchService,
      DeviationIncidentRepository incidentRepository) {
    this.contextualAdvisor = contextualAdvisor;
    this.batchService = batchService;
    this.incidentRepository = incidentRepository;
  }

  public Map<String, Object> verifyDeviation(Coordinate currentPosition,
      Coordinate nextStop,
      String stopAddress,
      String driverName,
      String city) {
    Map<String, Object> result = new HashMap<>();
    result.put("driver", driverName);
    result.put("nextStop", stopAddress);
    result.put("currentLat", currentPosition.getLat());
    result.put("currentLng", currentPosition.getLng());

    // 1. Check if driver is in a "Stop Zone" (near ANY pending order in their
    // batch)
    var activeBatchOpt = batchService.findActiveBatchByDriverName(driverName);
    if (activeBatchOpt.isPresent()) {
      var batch = activeBatchOpt.get();
      boolean inStopZone = batch.getOrders().stream()
          .filter(o -> !com.routeoptimizer.model.enums.OrderStatus.DELIVERED.equals(o.getStatus()))
          .anyMatch(o -> calculateHaversineDistance(
              currentPosition.getLat(), currentPosition.getLng(),
              o.getLat(), o.getLng()) < STOP_ZONE_RADIUS_METERS);

      if (inStopZone) {
        log.debug("Repartidor {} en Zona de Parada. Ignorando validación de desvío.", driverName);
        result.put("deviated", false);
        result.put("status", "IN_STOP_ZONE");
        result.put("alert", "Operando en zona de entrega. No se requiere alerta de desvío.");
        resolveIncidentIfExists(batch.getDriver().getId());
        return result;
      }
    }

    // 2. Geometric Deviation Check for NEXT stop
    double distance = calculateHaversineDistance(
        currentPosition.getLat(), currentPosition.getLng(),
        nextStop.getLat(), nextStop.getLng());

    result.put("distanceMeters", Math.round(distance));

    if (distance > DEVIATION_THRESHOLD_METERS) {
      log.warn("⚠️ DESVIACIÓN: {} a {}m de {} (umbral: {}m)",
          driverName, (int) distance, stopAddress, (int) DEVIATION_THRESHOLD_METERS);

      result.put("deviated", true);

      // 3. Incident Lifecycle & AI Control
      handleDeviationIncident(result, driverName, distance, stopAddress, city, activeBatchOpt);

    } else {
      result.put("deviated", false);
      result.put("alert", String.format("%s en ruta hacia %s (%dm)",
          driverName, stopAddress, (int) distance));

      // Resolve incident if they are back on track
      activeBatchOpt.ifPresent(b -> resolveIncidentIfExists(b.getDriver().getId()));
    }

    return result;
  }

  private void handleDeviationIncident(Map<String, Object> result, String driverName,
      double distance, String stopAddress, String city,
      java.util.Optional<com.routeoptimizer.model.entity.Batch> batchOpt) {

    Long driverId = batchOpt.map(b -> b.getDriver().getId()).orElse(0L);
    Long batchId = batchOpt.map(com.routeoptimizer.model.entity.Batch::getId).orElse(0L);

    var incidentOpt = incidentRepository.findByDriverIdAndStatus(driverId, "ACTIVE");

    if (incidentOpt.isEmpty()) {
      // START NEW INCIDENT + AI CALL
      DeviationIncident incident = new DeviationIncident(batchId, driverId,
          result.get("currentLat") != null ? (Double) result.get("currentLat") : 0.0,
          result.get("currentLng") != null ? (Double) result.get("currentLng") : 0.0);
      incident.setMaxDistanceMeters(distance);
      incidentRepository.save(incident);

      String alert = contextualAdvisor.generateDeviationInstruction(driverName, distance, stopAddress, city);
      result.put("alert", alert);
      result.put("incidentId", incident.getId());
    } else {
      // UPDATE EXISTING INCIDENT (NO AI CALL to save tokens)
      DeviationIncident incident = incidentOpt.get();
      if (distance > incident.getMaxDistanceMeters()) {
        incident.setMaxDistanceMeters(distance);
        incidentRepository.save(incident);
      }
      result.put("alert", "Sigues fuera de ruta. Tu jefe de logística ha sido notificado.");
      result.put("incidentId", incident.getId());
    }
  }

  private void resolveIncidentIfExists(Long driverId) {
    incidentRepository.findByDriverIdAndStatus(driverId, "ACTIVE").ifPresent(incident -> {
      incident.setStatus("RESOLVED");
      incident.setEndTime(java.time.LocalDateTime.now());
      incidentRepository.save(incident);
      log.info("Incidente de desvío para Driver #{} RESUELTO.", driverId);
    });
  }

  public double calculateHaversineDistance(double lat1, double lng1, double lat2, double lng2) {
    double dLat = Math.toRadians(lat2 - lat1);
    double dLng = Math.toRadians(lng2 - lng1);

    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
        + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.sin(dLng / 2) * Math.sin(dLng / 2);

    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return EARTH_RADIUS_METERS * c;
  }
}
