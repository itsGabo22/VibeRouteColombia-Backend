package com.routeoptimizer.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Genera automáticamente los perfiles de prueba de Pasto al iniciar la aplicación.
 * Se ejecuta después de que Hibernate ha verificado el esquema.
 */
@Component
public class UserSeeder {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    public UserSeeder(JdbcTemplate jdbcTemplate, PasswordEncoder passwordEncoder) {
        this.jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void seedUsers() {
        try {
            // Verificar si hay usuarios para evitar duplicados
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);

            if (count != null && count == 0) {
                System.out.println("👤 USER SEED: Iniciando creación de perfiles VibeRoute...");

                String hashedPass = passwordEncoder.encode("123");

                // 1. Admin Central
                jdbcTemplate.update(
                    "INSERT INTO users (email, password_hash, name, role, assigned_city) VALUES (?, ?, ?, ?, ?)",
                    "admin@viberoute.com", hashedPass, "Admin Central VibeRoute", "ADMIN", "BOGOTA"
                );

                // 2. Operador Logística Pasto
                jdbcTemplate.update(
                    "INSERT INTO users (email, password_hash, name, role, assigned_city) VALUES (?, ?, ?, ?, ?)",
                    "logistica.pasto@viberoute.com", hashedPass, "Operador Logística Pasto", "LOGISTICS", "Pasto"
                );

                // 3. Conductor Pasto (User part)
                jdbcTemplate.update(
                    "INSERT INTO users (email, password_hash, name, role, assigned_city) VALUES (?, ?, ?, ?, ?)",
                    "jose.pasto@viberoute.com", hashedPass, "José Cuarán (Pasto)", "DRIVER", "Pasto"
                );

                // 4. Conductor Pasto (Driver identity part)
                Long driverId = jdbcTemplate.queryForObject(
                    "SELECT id FROM users WHERE email = 'jose.pasto@viberoute.com'", Long.class
                );
                
                if (driverId != null) {
                    jdbcTemplate.update(
                        "INSERT INTO drivers (id, status, max_capacity, cost_per_hour, lat, lng) VALUES (?, ?, ?, ?, ?, ?)",
                        driverId, "AVAILABLE", 500, new java.math.BigDecimal("15000.00"), 1.2136, -77.2811
                    );
                }

                System.out.println("✅ DONE: Perfiles de Admin, Logística y Conductor listos en Pasto.");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Warning: No se pudieron precargar los usuarios: " + e.getMessage());
        }
    }
}
