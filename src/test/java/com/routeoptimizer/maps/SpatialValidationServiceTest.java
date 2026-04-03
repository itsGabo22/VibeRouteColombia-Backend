package com.routeoptimizer.maps;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.routeoptimizer.model.entity.CoverageZone;
import com.routeoptimizer.repository.CoverageZoneRepository;
import com.routeoptimizer.service.SpatialValidationService;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SpatialValidationServiceTest {

    @Mock
    private CoverageZoneRepository zoneRepo;

    @InjectMocks
    private SpatialValidationService spatialValidationService;

    private CoverageZone bogotaZone;

    @BeforeEach
    void setUp() {
        bogotaZone = new CoverageZone();
        bogotaZone.setName("Bogotá Urbana");
        bogotaZone.setCity("Bogotá");
    }

    @Test
    void validateCoordinate_Success_FallsInCorrectZone() {
        // Arrange
        when(zoneRepo.findZonesContaining(anyDouble(), anyDouble()))
                .thenReturn(List.of(bogotaZone));

        // Act
        boolean resultado = spatialValidationService.validateCoordinate(4.65, -74.05, "Bogotá");

        // Assert
        assertTrue(resultado, "La coordenada debería ser declarada válida al caer en la ciudad correcta");
    }

    @Test
    void validateCoordinate_Fails_DoesNotFallInAnyZone() {
        // Arrange
        when(zoneRepo.findZonesContaining(anyDouble(), anyDouble()))
                .thenReturn(Collections.emptyList());

        // Act
        boolean resultado = spatialValidationService.validateCoordinate(19.43, -99.13, "Bogotá");

        // Assert
        assertFalse(resultado, "La coordenada debería fallar si no cae en ninguna zona registrada");
    }

    @Test
    void validateCoordinate_Fails_FallsInWrongZone() {
        // Arrange
        CoverageZone caliZone = new CoverageZone();
        caliZone.setName("Cali Urbana");
        caliZone.setCity("Cali");

        when(zoneRepo.findZonesContaining(anyDouble(), anyDouble()))
                .thenReturn(List.of(caliZone));

        // Act
        boolean resultado = spatialValidationService.validateCoordinate(3.42, -76.52, "Bogotá");

        // Assert
        assertFalse(resultado,
                "La coordenada debería fallar si el polígono pertenece a otra ciudad diferente a la del pedido");
    }
}
