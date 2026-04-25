package com.routeoptimizer.controller;

import java.util.List;
import java.util.Map;

import com.routeoptimizer.model.entity.Batch;
import com.routeoptimizer.model.entity.Route;
import com.routeoptimizer.service.BatchService;
import com.routeoptimizer.service.RouteService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
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
    public ResponseEntity<List<Batch>> getPendingBatches(@org.springframework.web.bind.annotation.RequestParam(required = false) String city) {
        return ResponseEntity.ok(batchService.getPendingBatches(city));
    }

    @org.springframework.web.bind.annotation.PatchMapping("/{id}/manifest")
    public ResponseEntity<Batch> updateManifest(@PathVariable Long id, @org.springframework.web.bind.annotation.RequestBody Map<String, String> body) {
        String manifestUrl = body.get("manifestUrl");
        return ResponseEntity.ok(batchService.updateManifestUrl(id, manifestUrl));
    }

    @PostMapping("/manual")
    public ResponseEntity<Batch> createManualBatch(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Integer> idsRaw = (List<Integer>) body.get("orderIds");
        List<Long> orderIds = idsRaw.stream().map(Integer::longValue).collect(java.util.stream.Collectors.toList());
        String city = (String) body.get("city");
        return ResponseEntity.ok(batchService.createManualBatch(orderIds, city));
    }

    @PostMapping("/{batchId}/assign-driver/{driverId}")
    public ResponseEntity<Batch> assignDriverToBatch(@PathVariable Long batchId, @PathVariable Long driverId) {
        return ResponseEntity.ok(batchService.assignDriverToBatch(batchId, driverId));
    }
}
