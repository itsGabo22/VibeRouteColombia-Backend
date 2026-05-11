package com.routeoptimizer.service;

import com.routeoptimizer.model.entity.SystemAudit;
import com.routeoptimizer.repository.SystemAuditRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SystemAuditService {

    private static final int MAX_LOGS = 500;
    private final SystemAuditRepository auditRepository;

    public SystemAuditService(SystemAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    @Transactional
    public void log(String email, String action, String severity, String details) {
        // Guardar el nuevo log
        SystemAudit audit = new SystemAudit(email, action, severity, details);
        auditRepository.save(audit);

        // Pruning (Poda): Si superamos los 500, borramos los más antiguos
        long count = auditRepository.countAll();
        if (count > MAX_LOGS) {
            int toDelete = (int) (count - MAX_LOGS);
            List<Long> oldestIds = auditRepository.findOldestIds(PageRequest.of(0, toDelete));
            if (!oldestIds.isEmpty()) {
                auditRepository.deleteAllByIdInBatch(oldestIds);
            }
        }
    }

    public List<SystemAudit> getLatestLogs(int limit) {
        return auditRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, limit));
    }
}
