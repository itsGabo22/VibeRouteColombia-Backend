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
}
