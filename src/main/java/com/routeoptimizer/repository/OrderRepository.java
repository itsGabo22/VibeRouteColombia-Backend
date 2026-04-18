package com.routeoptimizer.repository;

import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.model.enums.OrderStatus;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA Repository for Order entity.
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

        List<Order> findByStatus(OrderStatus status);

        Optional<Order> findByUuid(UUID uuid);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("SELECT o FROM Order o WHERE o.batchId IS NULL")
        List<Order> findByBatchIdIsNullForUpdate();

        List<Order> findByBatchIdIsNull();

        @Query(value = "SELECT * FROM orders o WHERE ST_DWithin(" +
                        "CAST(ST_SetSRID(ST_MakePoint(o.lng, o.lat), 4326) AS geography), " + // Wait, should it
                                                                                              // correspond to lng
                                                                                              // and
                                                                                              // lat?
                        "CAST(ST_SetSRID(ST_MakePoint(:lng, :lat), 4326) AS geography), " +
                        ":radiusMeters)", nativeQuery = true)
        List<Order> findNearbyOrders(@Param("lat") double lat, @Param("lng") double lng,
                        @Param("radiusMeters") double radiusMeters);

        List<Order> findByCity(String city);

        long countByStatusAndCity(OrderStatus status, String city);

        @Query("SELECT COUNT(o) FROM Order o WHERE o.status = com.routeoptimizer.model.enums.OrderStatus.DELIVERED AND o.actualDeliveryTime >= :start")
        long countSuccessfulDeliveriesSince(@Param("start") java.time.LocalDateTime start);

        @Query("SELECT new com.routeoptimizer.dto.DriverRankingDTO(r.name, COUNT(o), 100.0, '') " +
                        "FROM Order o JOIN Batch b ON o.batchId = b.id JOIN b.driver r " +
                        "WHERE o.status = com.routeoptimizer.model.enums.OrderStatus.DELIVERED " +
                        "GROUP BY r.name " +
                        "ORDER BY COUNT(o) DESC")
        List<com.routeoptimizer.dto.DriverRankingDTO> getDriverRankings(
                        org.springframework.data.domain.Pageable pageable);

        @Query("SELECT SUM(o.price) FROM Order o WHERE o.status = com.routeoptimizer.model.enums.OrderStatus.DELIVERED")
        java.math.BigDecimal getTotalRevenue();

        @Query("SELECT SUM(o.price) FROM Order o WHERE o.status = com.routeoptimizer.model.enums.OrderStatus.DELIVERED AND o.city = :city")
        java.math.BigDecimal getRevenueByCity(@Param("city") String city);
}
