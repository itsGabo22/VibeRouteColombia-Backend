package com.routeoptimizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.routeoptimizer.model.entity.CoverageZone;

import java.util.List;

@Repository
public interface CoverageZoneRepository extends JpaRepository<CoverageZone, Long> {

    @Query(value = "SELECT * FROM coverage_zones z WHERE ST_Contains(z.geom, ST_SetSRID(ST_MakePoint(:lng, :lat), 4326))", nativeQuery = true)
    List<CoverageZone> findZonesContaining(@Param("lat") double lat, @Param("lng") double lng);

    List<CoverageZone> findByCity(String city);
}
