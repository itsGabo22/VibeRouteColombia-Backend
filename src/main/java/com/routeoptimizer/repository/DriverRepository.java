package com.routeoptimizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.routeoptimizer.model.entity.Driver;
import com.routeoptimizer.model.enums.DriverStatus;

import java.util.List;

/**
 * Repositorio de Spring Data JPA para la entidad Driver.
 */
@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

  // Método clave para buscar repartidores que puedan aceptar lotes
  List<Driver> findByStatus(DriverStatus status);
}
