package com.routeoptimizer.service;

import com.routeoptimizer.model.entity.Driver;
import com.routeoptimizer.model.entity.Batch;
import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.model.enums.DriverStatus;
import com.routeoptimizer.repository.BatchRepository;
import com.routeoptimizer.repository.DriverRepository;
import com.routeoptimizer.repository.OrderRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BatchService {

    private static final Logger log = LoggerFactory.getLogger(BatchService.class);
    private static final int MAX_ORDERS_PER_BATCH = 20;

    private final BatchRepository batchRepository;
    private final OrderRepository orderRepository;
    private final DriverRepository driverRepository;
    private final RouteService routeService;
    private final AIRouteSuggestionService aiService;

    public BatchService(BatchRepository batchRepository,
            OrderRepository orderRepository,
            DriverRepository driverRepository,
            RouteService routeService,
            AIRouteSuggestionService aiService) {
        this.batchRepository = batchRepository;
        this.orderRepository = orderRepository;
        this.driverRepository = driverRepository;
        this.routeService = routeService;
        this.aiService = aiService;
    }

    @Transactional
    public void addOrderToActiveBatch(Order order) {
        Optional<Batch> openBatch = batchRepository.findFirstByStatusOrderByCreationDateAsc("OPEN");

        Batch batch;
        if (openBatch.isPresent()) {
            batch = openBatch.get();
            log.info("Order #{} added to existing batch #{} ({}/{} orders)",
                    order.getId(), batch.getId(), batch.getOrders().size() + 1, MAX_ORDERS_PER_BATCH);
        } else {
            batch = new Batch();
            batch.setStatus("OPEN");
            batch = batchRepository.save(batch);
            log.info("New batch #{} created for order #{}", batch.getId(), order.getId());
        }

        order.setBatchId(batch.getId());
        orderRepository.save(order);
        batch.getOrders().add(order);

        if (batch.getOrders().size() >= MAX_ORDERS_PER_BATCH) {
            assignBatchToDriver(batch);
        }
    }

    private void assignBatchToDriver(Batch batch) {
        List<Driver> available = driverRepository.findByStatus(DriverStatus.AVAILABLE);

        if (available.isEmpty()) {
            if (batch.getOrders().size() >= MAX_ORDERS_PER_BATCH) {
                batch.setStatus("FULL_UNASSIGNED");
            }
            batchRepository.save(batch);
            log.warn("Batch #{} without available drivers ({} orders)", batch.getId(), batch.getOrders().size());
            return;
        }

        Driver driver = available.get(0);
        batch.setDriver(driver);
        batch.setStatus("ASSIGNED");
        batchRepository.save(batch);

        executeAIAnalysis(batch);

        try {
            routeService.optimizeAndSaveRoute(batch.getId());
        } catch (Exception e) {
            log.error("Error generating automatic route for batch #{}: {}", batch.getId(), e.getMessage());
        }

        log.info("✅ Batch #{} assigned to driver '{}' with {} orders.",
                batch.getId(), driver.getName(), batch.getOrders().size());
    }

    @Transactional
    public boolean assignAvailableDriverToOldestPendingBatch(Driver driver) {
        List<Batch> pendingBatches = batchRepository.findByDriverIsNullOrderByCreationDateAsc();

        if (pendingBatches.isEmpty()) {
            log.debug("No batches without driver to assign to '{}'", driver.getName());
            return false;
        }

        Batch batch = pendingBatches.get(0);
        batch.setDriver(driver);
        batch.setStatus("ASSIGNED");
        batchRepository.save(batch);

        executeAIAnalysis(batch);

        try {
            routeService.optimizeAndSaveRoute(batch.getId());
        } catch (Exception e) {
            log.error("Error generating automatic route for batch #{}: {}", batch.getId(), e.getMessage());
        }

        log.info("Driver '{}' automatically assigned to batch #{} ({} orders)",
                driver.getName(), batch.getId(), batch.getOrders().size());
        return true;
    }

    private void executeAIAnalysis(Batch batch) {
        try {
            // Note: need to make sure aiService accepts Batch if we renamed Lote to Batch
            // We use standard Object passing for now if methods are broken, or we will fix
            // AI service
            Map<String, Object> sugerencias = aiService.sugerirOptimizacion(batch);
            log.info("🤖 IA para Batch #{}: {}", batch.getId(), sugerencias.get("recomendacion"));
            log.info("   Tráfico: {}, Pedidos prioritarios: {}",
                    sugerencias.get("trafficInsights"),
                    sugerencias.get("pedidosPrioritariosSugeridos"));
        } catch (Exception e) {
            log.warn("Could not get AI suggestions for batch #{}: {}", batch.getId(), e.getMessage());
        }
    }

    @Transactional
    public int assignPendingBatches() {
        List<Batch> unassigned = batchRepository.findByDriverIsNullOrderByCreationDateAsc();
        int assignedCount = 0;
        for (Batch batch : unassigned) {
            assignBatchToDriver(batch);
            if ("ASSIGNED".equals(batch.getStatus()))
                assignedCount++;
        }
        return assignedCount;
    }

    @Transactional(readOnly = true)
    public List<Batch> findAll() {
        return batchRepository.findAll();
    }

    @Transactional(readOnly = true)
    @SuppressWarnings("null")
    public Batch findById(Long id) {
        return batchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Batch not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<Batch> getPendingBatches() {
        return batchRepository.findByDriverIsNullOrderByCreationDateAsc();
    }

    @Transactional(readOnly = true)
    public Optional<Batch> findActiveBatchByDriverName(String driverName) {
        return batchRepository.findAll().stream()
                .filter(b -> b.getDriver() != null && b.getDriver().getName().equalsIgnoreCase(driverName))
                .filter(b -> !"COMPLETED".equals(b.getStatus()))
                .findFirst();
    }
}
