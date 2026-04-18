package com.routeoptimizer.service;

import com.routeoptimizer.dto.OrderCreateDTO;
import com.routeoptimizer.model.Coordinate;
import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.model.enums.OrderStatus;
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
    order.setPriority(dto.getPriority());
    order.setTimeWindowStart(dto.getTimeWindowStart());
    order.setTimeWindowEnd(dto.getTimeWindowEnd());
    order.setClientReference(dto.getClientReference());
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
    order.setCity(dto.getCity());

    Order savedOrder = orderRepository.save(order);

    batchService.addOrderToActiveBatch(savedOrder);

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
  public List<Order> findPendingWithoutBatch() {
    return orderRepository.findByBatchIdIsNull();
  }

  @Transactional
  @SuppressWarnings("null")
  public Order updateStatus(Long id, OrderStatus newStatus, String reason) {
    Order order = findById(id);
    order.setStatus(newStatus);

    if (newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.RETURNED) {
      if (reason != null) {
        order.setNonDeliveryReason(reason);
      }
    } else {
      order.setNonDeliveryReason(null);
    }

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
  public List<Order> createOrdersBulk(List<OrderCreateDTO> dtos) {
    return dtos.stream()
        .map(this::createOrder)
        .collect(Collectors.toList());
  }
}
