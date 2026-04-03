package com.routeoptimizer.dto;

import com.routeoptimizer.model.Coordinate;
import com.routeoptimizer.model.entity.Order;
import com.routeoptimizer.model.enums.OrderStatus;
import com.routeoptimizer.model.enums.Priority;

import java.time.LocalDate;
import java.time.LocalTime;

public class OrderResponseDTO {

  private Long id;
  private String address;
  private Coordinate location;
  private Priority priority;
  private OrderStatus status;
  private LocalDate date;
  private LocalTime timeWindowStart;
  private LocalTime timeWindowEnd;
  private String clientReference;
  private String city;
  private String nonDeliveryReason;

  public static OrderResponseDTO fromEntity(Order order) {
    if (order == null)
      return null;

    OrderResponseDTO dto = new OrderResponseDTO();
    dto.setId(order.getId());
    dto.setAddress(order.getAddress());
    dto.setLocation(order.getLocation());
    dto.setPriority(order.getPriority());
    dto.setStatus(order.getStatus());
    dto.setDate(order.getDate());
    dto.setTimeWindowStart(order.getTimeWindowStart());
    dto.setTimeWindowEnd(order.getTimeWindowEnd());
    dto.setClientReference(order.getClientReference());
    dto.setCity(order.getCity());
    dto.setNonDeliveryReason(order.getNonDeliveryReason());
    return dto;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public String getNonDeliveryReason() {
    return nonDeliveryReason;
  }

  public void setNonDeliveryReason(String nonDeliveryReason) {
    this.nonDeliveryReason = nonDeliveryReason;
  }
}
