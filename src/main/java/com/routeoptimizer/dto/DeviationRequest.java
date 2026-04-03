package com.routeoptimizer.dto;

import com.routeoptimizer.model.Coordinate;

/**
 * DTO for deviation detection requests.
 * Uses typed fields instead of generic maps to clean up Controller logic.
 */
public class DeviationRequest {
    private Double currentLat;
    private Double currentLng;
    private Double stopLat;
    private Double stopLng;
    private String stopAddress;
    private String driverName;
    private String city;

    // Getters and Setters
    public Double getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(Double currentLat) {
        this.currentLat = currentLat;
    }

    public Double getCurrentLng() {
        return currentLng;
    }

    public void setCurrentLng(Double currentLng) {
        this.currentLng = currentLng;
    }

    public Double getStopLat() {
        return stopLat;
    }

    public void setStopLat(Double stopLat) {
        this.stopLat = stopLat;
    }

    public Double getStopLng() {
        return stopLng;
    }

    public void setStopLng(Double stopLng) {
        this.stopLng = stopLng;
    }

    public String getStopAddress() {
        return stopAddress;
    }

    public void setStopAddress(String stopAddress) {
        this.stopAddress = stopAddress;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Coordinate getCurrentCoordinate() {
        return new Coordinate(currentLat, currentLng);
    }

    public Coordinate getStopCoordinate() {
        return new Coordinate(stopLat, stopLng);
    }
}
