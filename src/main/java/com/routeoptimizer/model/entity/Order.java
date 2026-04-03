package com.routeoptimizer.model.entity;

import com.routeoptimizer.model.Coordinate;
import com.routeoptimizer.model.enums.OrderStatus;
import com.routeoptimizer.model.enums.Priority;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Order entity mapped to the database.
 */
@Entity
@Table(name = "orders")
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, updatable = false)
  private UUID uuid = UUID.randomUUID();

  @Column(nullable = false)
  private String address;

  @Embedded
  private Coordinate location;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Priority priority;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status;

  @Column(nullable = false)
  private LocalDate date;

  @Column(name = "time_window_start")
  private LocalTime timeWindowStart;

  @Column(name = "time_window_end")
  private LocalTime timeWindowEnd;

  @Column(name = "client_reference")
  private String clientReference;

  @Column(nullable = false)
  private String city;

  @Column(name = "batch_id")
  private Long batchId;

  @Column(name = "delivery_order")
  private Integer deliveryOrder;

  @Column(name = "actual_delivery_time")
  private LocalDateTime actualDeliveryTime;

  @Column(name = "delivery_lat")
  private Double deliveryLat;

  @Column(name = "delivery_lng")
  private Double deliveryLng;

  @Column(name = "non_delivery_reason")
  private String nonDeliveryReason;

  public Order() {
  }

  public Double getLat() {
    return location != null ? location.getLat() : null;
  }

  public Double getLng() {
    return location != null ? location.getLng() : null;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public UUID getUuid() {
    return uuid;
  }

  public void setUuid(UUID uuid) {
    this.uuid = uuid;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public Coordinate getLocation() {
    return location;
  }

  public void setLocation(Coordinate location) {
    this.location = location;
  }

  public Priority getPriority() {
    return priority;
  }

  public void setPriority(Priority priority) {
    this.priority = priority;
  }

  public OrderStatus getStatus() {
    return status;
  }

  public void setStatus(OrderStatus status) {
    this.status = status;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public LocalTime getTimeWindowStart() {
    return timeWindowStart;
  }

  public void setTimeWindowStart(LocalTime timeWindowStart) {
    this.timeWindowStart = timeWindowStart;
  }

  public LocalTime getTimeWindowEnd() {
    return timeWindowEnd;
  }

  public void setTimeWindowEnd(LocalTime timeWindowEnd) {
    this.timeWindowEnd = timeWindowEnd;
  }

  public String getClientReference() {
    return clientReference;
  }

  public void setClientReference(String clientReference) {
    this.clientReference = clientReference;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
  }

  public Long getBatchId() {
    return batchId;
  }

  public void setBatchId(Long batchId) {
    this.batchId = batchId;
  }

  public Integer getDeliveryOrder() {
    return deliveryOrder;
  }

  public void setDeliveryOrder(Integer deliveryOrder) {
    this.deliveryOrder = deliveryOrder;
  }

  public LocalDateTime getActualDeliveryTime() {
    return actualDeliveryTime;
  }

  public void setActualDeliveryTime(LocalDateTime actualDeliveryTime) {
    this.actualDeliveryTime = actualDeliveryTime;
  }

  public Double getDeliveryLat() {
    return deliveryLat;
  }

  public void setDeliveryLat(Double deliveryLat) {
    this.deliveryLat = deliveryLat;
  }

  public Double getDeliveryLng() {
    return deliveryLng;
  }

  public void setDeliveryLng(Double deliveryLng) {
    this.deliveryLng = deliveryLng;
  }

  public String getNonDeliveryReason() {
    return nonDeliveryReason;
  }

  public void setNonDeliveryReason(String nonDeliveryReason) {
    this.nonDeliveryReason = nonDeliveryReason;
  }
}
