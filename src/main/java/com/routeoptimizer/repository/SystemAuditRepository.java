package com.routeoptimizer.repository;

import com.routeoptimizer.model.entity.SystemAudit;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemAuditRepository extends JpaRepository<SystemAudit, Long> {
    
    List<SystemAudit> findAllByOrderByTimestampDesc(Pageable pageable);

    @Query("SELECT COUNT(s) FROM SystemAudit s")
    long countAll();

    @Query("SELECT s.id FROM SystemAudit s ORDER BY s.timestamp ASC")
    List<Long> findOldestIds(Pageable pageable);
}
