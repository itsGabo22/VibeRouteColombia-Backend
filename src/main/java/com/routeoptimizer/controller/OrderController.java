package com.routeoptimizer.controller;

import com.routeoptimizer.dto.OrderCreateDTO;
import com.routeoptimizer.dto.OrderResponseDTO;
import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.model.enums.OrderStatus;
import com.routeoptimizer.service.OrderService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

  private final OrderService orderService;

  public OrderController(OrderService orderService) {
    this.orderService = orderService;
  }

  @PostMapping
  public ResponseEntity<?> createOrder(@RequestBody OrderCreateDTO dto) {
    try {
      Order createdOrder = orderService.createOrder(dto);
      return new ResponseEntity<>(OrderResponseDTO.fromEntity(createdOrder), HttpStatus.CREATED);
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error", e.getMessage()));
    }
  }

  @GetMapping
  public ResponseEntity<List<OrderResponseDTO>> listAll() {
    List<OrderResponseDTO> orders = orderService.findAll().stream()
        .map(OrderResponseDTO::fromEntity)
        .collect(Collectors.toList());
    return ResponseEntity.ok(orders);
  }

  @GetMapping("/pending")
  public ResponseEntity<List<OrderResponseDTO>> listPendingWithoutBatch(@RequestParam(required = false) String city) {
    List<OrderResponseDTO> orders = orderService.findPendingWithoutBatch(city).stream()
        .map(OrderResponseDTO::fromEntity)
        .collect(Collectors.toList());
    return ResponseEntity.ok(orders);
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderResponseDTO> getOrder(@PathVariable Long id) {
    Order order = orderService.findById(id);
    return ResponseEntity.ok(OrderResponseDTO.fromEntity(order));
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<OrderResponseDTO> updateStatus(
      @PathVariable Long id,
      @RequestParam OrderStatus status,
      @RequestParam(required = false) String reason) {
    Order updated = orderService.updateStatus(id, status, reason);
    return ResponseEntity.ok(OrderResponseDTO.fromEntity(updated));
  }

  @GetMapping("/city/{city}")
  public ResponseEntity<List<OrderResponseDTO>> listByCity(@PathVariable String city) {
    List<OrderResponseDTO> orders = orderService.findByCity(city).stream()
        .map(OrderResponseDTO::fromEntity)
        .collect(Collectors.toList());
    return ResponseEntity.ok(orders);
  }

  @GetMapping("/city/{city}/count")
  public ResponseEntity<Map<String, Object>> getCountByStatusAndCity(
      @PathVariable String city,
      @RequestParam OrderStatus status) {
    long count = orderService.countByStatusAndCity(status, city);
    return ResponseEntity.ok(Map.of(
        "city", city,
        "status", status,
        "count", count));
  }

  @PostMapping("/bulk")
  public ResponseEntity<?> createOrdersBulk(@RequestBody List<OrderCreateDTO> dtos) {
    try {
      Map<String, Object> result = orderService.createOrdersBulk(dtos);
      int createdCount = (int) result.get("createdCount");

      if (createdCount == 0) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
      }

      return ResponseEntity.status(HttpStatus.CREATED).body(result);
    } catch (RuntimeException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(Map.of("error", e.getMessage()));
    }
  }
}
