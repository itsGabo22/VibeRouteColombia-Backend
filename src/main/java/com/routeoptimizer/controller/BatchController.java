package com.routeoptimizer.controller;

import java.util.List;
import java.util.Map;

import com.routeoptimizer.model.entity.Batch;
import com.routeoptimizer.model.entity.Route;
import com.routeoptimizer.service.BatchService;
import com.routeoptimizer.service.RouteService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/batches")
public class BatchController {

    private final BatchService batchService;
    private final RouteService routeService;

    public BatchController(BatchService batchService, RouteService routeService) {
        this.batchService = batchService;
        this.routeService = routeService;
    }

    @GetMapping
    public ResponseEntity<List<Batch>> listAll() {
        return ResponseEntity.ok(batchService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Batch> getBatch(@PathVariable Long id) {
        return ResponseEntity.ok(batchService.findById(id));
    }

    @PostMapping("/assign-pending")
    public ResponseEntity<Map<String, Integer>> assignPendingBatches() {
        int total = batchService.assignPendingBatches();
        return ResponseEntity.ok(Map.of("batchesAssigned", total));
    }

    @PostMapping("/{id}/optimize")
    public ResponseEntity<Route> optimizeBatch(@PathVariable Long id) {
        return ResponseEntity.ok(routeService.optimizeAndSaveRoute(id));
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Batch>> getPendingBatches() {
        return ResponseEntity.ok(batchService.getPendingBatches());
    }
}
