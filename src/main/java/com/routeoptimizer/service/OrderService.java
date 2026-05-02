package com.routeoptimizer.service;

import com.routeoptimizer.dto.OrderCreateDTO;
import com.routeoptimizer.dto.OrderResponseDTO;
import com.routeoptimizer.model.Coordinate;
import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.model.enums.OrderStatus;
import com.routeoptimizer.model.enums.Priority;
import com.routeoptimizer.repository.OrderRepository;

import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

  private final OrderRepository orderRepository;
  private final MapService mapService;
  private final BatchService batchService;
  private final SpatialValidationService spatialValidationService;
  private final SimpMessagingTemplate messagingTemplate;

  public OrderService(OrderRepository orderRepository,
      MapService mapService,
      @Lazy BatchService batchService,
      SpatialValidationService spatialValidationService,
      SimpMessagingTemplate messagingTemplate) {
    this.orderRepository = orderRepository;
    this.mapService = mapService;
    this.batchService = batchService;
    this.spatialValidationService = spatialValidationService;
    this.messagingTemplate = messagingTemplate;
  }

  @Transactional
  public Order createOrder(OrderCreateDTO dto) {
    Order order = new Order();
    order.setAddress(dto.getAddress());
    order.setPriority(dto.getPriority() != null ? dto.getPriority() : Priority.MEDIUM);
    order.setTimeWindowStart(dto.getTimeWindowStart());
    order.setTimeWindowEnd(dto.getTimeWindowEnd());
    order.setClientReference(dto.getClientReference());
    order.setClientName(dto.getClientName());
    order.setPrice(dto.getPrice());

    order.setStatus(OrderStatus.PENDING);
    order.setDate(LocalDate.now());

    Coordinate coord;
    if (dto.getLocation() == null || dto.getLocation().getLat() == null) {
      coord = mapService.geocode(dto.getAddress(), dto.getCity());
    } else {
      coord = dto.getLocation();
    }

    if (!spatialValidationService.validateCoordinate(coord.getLat(), coord.getLng(), dto.getCity())) {
      throw new RuntimeException("La dirección '" + dto.getAddress()
          + "' no se pudo ubicar dentro de la zona de cobertura de " + dto.getCity());
    }

    order.setLocation(coord);
    order.setCity(dto.getCity() != null ? dto.getCity().trim() : "Bogotá");

    Order savedOrder = orderRepository.save(order);
    // batchService.addOrderToActiveBatch(savedOrder); // Deshabilitado para
    // permitir consolidación manual por Logística
    return savedOrder;
  }

  @Transactional(readOnly = true)
  public List<Order> findAll() {
    return orderRepository.findAll();
  }

  @Transactional(readOnly = true)
  @SuppressWarnings("null")
  public Order findById(Long id) {
    return orderRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Order no encontrado con ID: " + id));
  }

  @Transactional(readOnly = true)
  public List<Order> findPendingWithoutBatch(String city) {
    if (city != null && !city.isEmpty()) {
      return orderRepository.findByBatchIdIsNullAndCity(city);
    }
    return orderRepository.findByBatchIdIsNull();
  }

  @Transactional
  @SuppressWarnings("null")
  public Order updateStatus(Long id, OrderStatus newStatus, String reason) {
    Order order = findById(id);

    // Patrón State: Delegamos la transición al estado actual
    order.getStateObject().transitionTo(order, newStatus, reason);

    Order savedOrder = orderRepository.save(order);

    messagingTemplate.convertAndSend("/topic/logistica", Map.of(
        "orderId", savedOrder.getId(),
        "newStatus", savedOrder.getStatus(),
        "city", savedOrder.getCity(),
        "reason", savedOrder.getNonDeliveryReason() != null ? savedOrder.getNonDeliveryReason() : ""));

    return savedOrder;
  }

  @Transactional(readOnly = true)
  public List<Order> findByCity(String city) {
    return orderRepository.findByCity(city);
  }

  @Transactional(readOnly = true)
  public long countByStatusAndCity(OrderStatus status, String city) {
    return orderRepository.countByStatusAndCity(status, city);
  }

  @Transactional
  public Map<String, Object> createOrdersBulk(List<OrderCreateDTO> dtos) {
    List<Order> created = new java.util.ArrayList<>();
    List<Map<String, String>> errors = new java.util.ArrayList<>();

    for (int i = 0; i < dtos.size(); i++) {
      OrderCreateDTO dto = dtos.get(i);
      try {
        created.add(createOrder(dto));
      } catch (RuntimeException e) {
        String ref = dto.getClientReference() != null ? dto.getClientReference() : "index-" + i;
        errors.add(Map.of("reference", ref, "error", e.getMessage()));
      }
    }

    return Map.of(
        "created", created,
        "createdCount", created.size(),
        "errorCount", errors.size(),
        "errors", errors);
  }

  @Transactional(readOnly = true)
  public OrderResponseDTO enrichOrderResponse(Order order) {
    OrderResponseDTO dto = OrderResponseDTO.fromEntity(order);
    if (order.getBatchId() != null) {
      try {
        com.routeoptimizer.model.entity.Batch batch = batchService.findById(order.getBatchId());
        if (batch != null && batch.getDriver() != null) {
          dto.setDriverName(batch.getDriver().getName());
        }
      } catch (Exception e) {
        // Ignoramos si el lote no existe, simplemente no poblamos el driver
      }
    }
    return dto;
  }
}

