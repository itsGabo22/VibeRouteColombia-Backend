package com.routeoptimizer.dto;

import com.routeoptimizer.model.entity.Driver;
import com.routeoptimizer.model.Coordinate;
import com.routeoptimizer.model.enums.DriverStatus;

public class DriverResponseDTO {

  private Long id;
  private String email;
  private String name;
  private String phone;
  private Coordinate location;
  private DriverStatus status;
  private Integer maxCapacity;
  private java.math.BigDecimal costPerHour;
  private Integer completedOrders;
  private Integer failedOrders;
  private String assignedCity;
  private Long currentBatchId;
  private Integer currentOrdersCount;
  private java.util.List<String> activeAddresses;

  public static DriverResponseDTO fromEntity(Driver driver) {
    if (driver == null)
      return null;

    DriverResponseDTO dto = new DriverResponseDTO();
    dto.setId(driver.getId());
    dto.setEmail(driver.getEmail());
    dto.setName(driver.getName());
    dto.setPhone(driver.getPhone());
    dto.setLocation(driver.getLocation());
    dto.setStatus(driver.getStatus());
    dto.setMaxCapacity(driver.getMaxCapacity());
    dto.setCostPerHour(driver.getCostPerHour());
    dto.setCompletedOrders(driver.getCompletedOrders());
    dto.setFailedOrders(driver.getFailedOrders());
    dto.setAssignedCity(driver.getAssignedCity());
    return dto;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public Coordinate getLocation() {
    return location;
  }

  public void setLocation(Coordinate location) {
    this.location = location;
  }

  public DriverStatus getStatus() {
    return status;
  }

  public void setStatus(DriverStatus status) {
    this.status = status;
  }

  public Integer getMaxCapacity() {
    return maxCapacity;
  }

  public void setMaxCapacity(Integer maxCapacity) {
    this.maxCapacity = maxCapacity;
  }

  public java.math.BigDecimal getCostPerHour() {
    return costPerHour;
  }

  public void setCostPerHour(java.math.BigDecimal costPerHour) {
    this.costPerHour = costPerHour;
  }

  public Integer getCompletedOrders() {
    return completedOrders;
  }

  public void setCompletedOrders(Integer completedOrders) {
    this.completedOrders = completedOrders;
  }

  public Integer getFailedOrders() {
    return failedOrders;
  }

  public void setFailedOrders(Integer failedOrders) {
    this.failedOrders = failedOrders;
  }

  public String getAssignedCity() {
    return assignedCity;
  }

  public void setAssignedCity(String assignedCity) {
    this.assignedCity = assignedCity;
  }

  public Long getCurrentBatchId() {
    return currentBatchId;
  }

  public void setCurrentBatchId(Long currentBatchId) {
    this.currentBatchId = currentBatchId;
  }

  public Integer getCurrentOrdersCount() {
    return currentOrdersCount;
  }

  public void setCurrentOrdersCount(Integer currentOrdersCount) {
    this.currentOrdersCount = currentOrdersCount;
  }

  public java.util.List<String> getActiveAddresses() {
    return activeAddresses;
  }

  public void setActiveAddresses(java.util.List<String> activeAddresses) {
    this.activeAddresses = activeAddresses;
  }
}
