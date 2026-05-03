package com.routeoptimizer.service;

import com.routeoptimizer.dto.DriverCreateDTO;
import com.routeoptimizer.model.Coordinate;
import com.routeoptimizer.model.entity.Driver;
import com.routeoptimizer.model.enums.DriverStatus;
import com.routeoptimizer.model.enums.Role;
import com.routeoptimizer.repository.DriverRepository;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.routeoptimizer.model.entity.Batch;
import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.dto.DriverResponseDTO;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio con la lógica de negocio para Drivers.
 */
@Service
public class DriverService {

  private final DriverRepository driverRepository;
  private final BatchService batchService;

  public DriverService(DriverRepository driverRepository, @Lazy BatchService batchService) {
    this.driverRepository = driverRepository;
    this.batchService = batchService;
  }

  @Transactional
  public Driver createDriver(DriverCreateDTO dto) {
    Driver driver = new Driver();
    driver.setEmail(dto.getEmail());
    driver.setPasswordHash(dto.getPassword() != null ? dto.getPassword() : "defaultHash");
    driver.setName(dto.getName());
    driver.setPhone(dto.getPhone());
    driver.setRole(Role.DRIVER);

    driver.setMaxCapacity(dto.getMaxCapacity());
    driver.setCostPerHour(dto.getCostPerHour());
    driver.setStatus(DriverStatus.INACTIVE);

    return driverRepository.save(driver);
  }

  @Transactional(readOnly = true)
  public List<Driver> findAll() {
    return driverRepository.findAll();
  }

  @Transactional(readOnly = true)
  @SuppressWarnings("null")
  public Driver findById(Long id) {
    return driverRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Driver no encontrado con ID: " + id));
  }

  @Transactional(readOnly = true)
  public List<Driver> findAvailable() {
    return driverRepository.findByStatus(DriverStatus.AVAILABLE);
  }

  @Transactional
  public Driver changeStatus(Long id, DriverStatus newStatus) {
    Driver driver = findById(id);
    driver.setStatus(newStatus);
    Driver saved = driverRepository.save(driver);

    // Al pasar a DISPONIBLE, asignar automáticamente al lote pendiente más antiguo
    if (newStatus == DriverStatus.AVAILABLE) {
      batchService.assignAvailableDriverToOldestPendingBatch(saved);
    }

    return saved;
  }

  @Transactional
  public Driver updateLocation(Long id, Coordinate coord) {
    Driver driver = findById(id);
    driver.updateLocation(coord.getLat(), coord.getLng());
    return driverRepository.save(driver);
  }

  /**
   * Asigna manualmente un repartidor al lote más antiguo sin repartidor.
   */
  @Transactional
  public Driver assignDriverToBatch(Long driverId) {
    Driver driver = findById(driverId);
    driver.setStatus(DriverStatus.ON_ROUTE);

    batchService.assignAvailableDriverToOldestPendingBatch(driver);

    return driverRepository.save(driver);
  }

  @Transactional(readOnly = true)
  public List<DriverResponseDTO> getFleetStatus() {
    List<Driver> drivers = driverRepository.findAll();
    System.out.println("[FLEET-STATUS] Total drivers encontrados en BD: " + drivers.size());
    drivers.forEach(d -> System.out.println("  -> Driver ID=" + d.getId() + " name=" + d.getName() + " status=" + d.getStatus() + " city=" + d.getAssignedCity()));

    List<DriverResponseDTO> result = new java.util.ArrayList<>();
    for (Driver d : drivers) {
      try {
        DriverResponseDTO dto = DriverResponseDTO.fromEntity(d);
        
        // Buscar carga activa por ID del conductor (más fiable que por nombre)
        batchService.findActiveBatchByDriverId(d.getId()).ifPresent(batch -> {
          dto.setCurrentBatchId(batch.getId());
          dto.setCurrentOrdersCount(batch.getOrders() != null ? batch.getOrders().size() : 0);
          dto.setActiveAddresses(batch.getOrders() != null 
              ? batch.getOrders().stream()
                  .map(Order::getAddress)
                  .limit(3)
                  .collect(Collectors.toList())
              : java.util.Collections.emptyList());
        });
        
        result.add(dto);
      } catch (Exception e) {
        System.err.println("[FLEET-STATUS] Error procesando driver ID=" + d.getId() + ": " + e.getMessage());
        // Aún así lo agregamos con datos básicos para que nunca desaparezca
        DriverResponseDTO fallback = DriverResponseDTO.fromEntity(d);
        result.add(fallback);
      }
    }
    return result;
  }
}
