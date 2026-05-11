package com.routeoptimizer.dto;

import java.util.List;
import com.routeoptimizer.model.entity.Order;

public class ClusterDTO {
    private Long suggestedDriverId;
    private String suggestedDriverName;
    private String driverStatus;
    private List<Order> orders;
    private Double centroidLat;
    private Double centroidLng;
    private String zoneName;

    public ClusterDTO() {}

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public Long getSuggestedDriverId() {
        return suggestedDriverId;
    }

    public void setSuggestedDriverId(Long suggestedDriverId) {
        this.suggestedDriverId = suggestedDriverId;
    }

    public String getSuggestedDriverName() {
        return suggestedDriverName;
    }

    public void setSuggestedDriverName(String suggestedDriverName) {
        this.suggestedDriverName = suggestedDriverName;
    }

    public String getDriverStatus() {
        return driverStatus;
    }

    public void setDriverStatus(String driverStatus) {
        this.driverStatus = driverStatus;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public Double getCentroidLat() {
        return centroidLat;
    }

    public void setCentroidLat(Double centroidLat) {
        this.centroidLat = centroidLat;
    }

    public Double getCentroidLng() {
        return centroidLng;
    }

    public void setCentroidLng(Double centroidLng) {
        this.centroidLng = centroidLng;
    }
}
