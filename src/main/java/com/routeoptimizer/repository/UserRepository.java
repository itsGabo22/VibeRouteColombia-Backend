package com.routeoptimizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.routeoptimizer.model.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByEmail(String email);
  Optional<User> findByEmailIgnoreCase(String email);

  // Escalar nativo: Solo obtiene el estado 'enabled' físico para evitar el error clazz_ de herencia JOINED
  @org.springframework.data.jpa.repository.Query(value = "SELECT enabled FROM users WHERE email = ?1 LIMIT 1", nativeQuery = true)
  Optional<Boolean> checkPhysicalEnabledStatus(String email);
}
