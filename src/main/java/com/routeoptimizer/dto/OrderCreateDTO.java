package com.routeoptimizer.dto;

import com.routeoptimizer.model.Coordinate;
import com.routeoptimizer.model.enums.Priority;

import java.time.LocalTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderCreateDTO {

  private String address;
  private String city;
  private Coordinate location;
  private Priority priority = Priority.MEDIUM;
  private LocalTime timeWindowStart;
  private LocalTime timeWindowEnd;
  private String clientReference;
  private java.math.BigDecimal price;

  // Getters and Setters
  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    this.city = city;
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

  public java.math.BigDecimal getPrice() {
    return price;
  }

  public void setPrice(java.math.BigDecimal price) {
    this.price = price;
  }
}
