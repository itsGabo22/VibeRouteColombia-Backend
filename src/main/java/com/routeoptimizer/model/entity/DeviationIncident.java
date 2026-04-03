package com.routeoptimizer.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity to track route deviation incidents for logistics reporting.
 */
@Entity
@Table(name = "deviation_incidents")
public class DeviationIncident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "batch_id", nullable = false)
    private Long batchId;

    @Column(name = "driver_id", nullable = false)
    private Long driverId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "start_lat")
    private Double startLat;

    @Column(name = "start_lng")
    private Double startLng;

    @Column(name = "max_distance_meters")
    private Double maxDistanceMeters;

    @Column(name = "driver_justification", length = 500)
    private String driverJustification;

    @Column(name = "status")
    private String status; // ACTIVE, RESOLVED

    public DeviationIncident() {
    }

    public DeviationIncident(Long batchId, Long driverId, Double startLat, Double startLng) {
        this.batchId = batchId;
        this.driverId = driverId;
        this.startLat = startLat;
        this.startLng = startLng;
        this.startTime = LocalDateTime.now();
        this.status = "ACTIVE";
        this.maxDistanceMeters = 0.0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getBatchId() {
        return batchId;
    }

    public void setBatchId(Long batchId) {
        this.batchId = batchId;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Double getStartLat() {
        return startLat;
    }

    public void setStartLat(Double startLat) {
        this.startLat = startLat;
    }

    public Double getStartLng() {
        return startLng;
    }

    public void setStartLng(Double startLng) {
        this.startLng = startLng;
    }

    public Double getMaxDistanceMeters() {
        return maxDistanceMeters;
    }

    public void setMaxDistanceMeters(Double maxDistanceMeters) {
        this.maxDistanceMeters = maxDistanceMeters;
    }

    public String getDriverJustification() {
        return driverJustification;
    }

    public void setDriverJustification(String driverJustification) {
        this.driverJustification = driverJustification;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
