package com.routeoptimizer.dto;

public class DriverCreateDTO {

  private String email;
  private String password;
  private String name;
  private String phone;
  private Integer maxCapacity;
  private java.math.BigDecimal costPerHour;

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
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
}
