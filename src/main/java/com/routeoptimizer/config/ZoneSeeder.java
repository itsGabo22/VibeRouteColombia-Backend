package com.routeoptimizer.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * ZoneSeeder ensures the logistics polygons are available in the database.
 * This class is located in the routes package as requested by the user.
 */
@Component
public class ZoneSeeder {

    private final JdbcTemplate jdbcTemplate;

    public ZoneSeeder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Initializes the default Colombia coverage zones if the table is empty.
     */
    @PostConstruct
    public void seedZones() {
        try {
            Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM coverage_zones", Integer.class);

            if (count != null && count == 0) {
                System.out.println("🚩 LOGISTICS SEED: Inserting coverage polygons for Colombia...");

                String sql = "INSERT INTO coverage_zones (name, city, geom) VALUES " +
                        "('Bogotá Urbana', 'Bogotá', ST_GeomFromText('POLYGON((-74.22 4.47, -74.22 4.84, -73.98 4.84, -73.98 4.47, -74.22 4.47))', 4326)), "
                        +
                        "('Medellín Urbana', 'Medellín', ST_GeomFromText('POLYGON((-75.66 6.17, -75.66 6.34, -75.50 6.34, -75.50 6.17, -75.66 6.17))', 4326)), "
                        +
                        "('Pasto Urbana', 'Pasto', ST_GeomFromText('POLYGON((-77.34 1.18, -77.34 1.25, -77.24 1.25, -77.24 1.18, -77.34 1.18))', 4326))";

                jdbcTemplate.execute(sql);
                System.out.println("✅ DONE: Bogota, Medellin, and Pasto polygons are now active.");
            }
        } catch (Exception e) {
            System.err
                    .println("⚠️ Warning: Could not seed coverage zones. Ensure PostGIS is active. " + e.getMessage());
        }
    }
}
