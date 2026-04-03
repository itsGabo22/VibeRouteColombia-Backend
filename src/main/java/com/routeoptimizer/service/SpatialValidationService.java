package com.routeoptimizer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.routeoptimizer.model.entity.CoverageZone;
import com.routeoptimizer.repository.CoverageZoneRepository;

import java.util.List;

/**
 * Servicio encargado de validar la ubicación de los pedidos contra las zonas
 * de cobertura definidas en PostGIS.
 */
@Service
public class SpatialValidationService {

    private static final Logger log = LoggerFactory.getLogger(SpatialValidationService.class);
    private final CoverageZoneRepository zonaRepo;

    public SpatialValidationService(CoverageZoneRepository zonaRepo) {
        this.zonaRepo = zonaRepo;
    }

    /**
     * Validates that a coordinate falls within a valid coverage zone
     * for the specified city.
     */
    public boolean validateCoordinate(double lat, double lng, String city) {
        // Basic normalization to avoid issues with accents or case
        String normalizedCity = city.replace("á", "a").replace("é", "e")
                .replace("í", "i").replace("ó", "o").replace("ú", "u");

        List<CoverageZone> zonas = zonaRepo.findZonesContaining(lat, lng);

        if (zonas.isEmpty()) {
            log.warn("⚠️ SPATIAL COLLISION: ({}, {}) does not belong to any registered zone.", lat, lng);
            return false;
        }

        // Verify that one of the zones found corresponds to the order city
        boolean cityCorrect = zonas.stream()
                .anyMatch(z -> {
                    String zCity = z.getCity().toLowerCase()
                            .replace("á", "a").replace("é", "e")
                            .replace("í", "i").replace("ó", "o").replace("ú", "u");
                    return zCity.contains(normalizedCity.toLowerCase());
                });

        if (!cityCorrect) {
            log.warn("⚠️ CITY COLLISION: Point falls in {}, but order requested {}",
                    zonas.get(0).getCity(), city);
            return false;
        }

        log.info("✅ Spatial validation successful for {}", city);
        return true;
    }
}
