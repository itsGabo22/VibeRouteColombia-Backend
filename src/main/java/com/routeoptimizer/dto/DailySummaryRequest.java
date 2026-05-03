package com.routeoptimizer.dto;

public class DailySummaryRequest {
    private int totalDelivered;
    private int totalPending;
    private int totalCancelled;
    private int totalReturned;
    private double totalHours;
    private int efficiencyScore;
    private String city;

    // Getters and Setters
    public int getTotalDelivered() {
        return totalDelivered;
    }

    public void setTotalDelivered(int totalDelivered) {
        this.totalDelivered = totalDelivered;
    }

    public int getTotalPending() {
        return totalPending;
    }

    public void setTotalPending(int totalPending) {
        this.totalPending = totalPending;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(double totalHours) {
        this.totalHours = totalHours;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getTotalCancelled() {
        return totalCancelled;
    }

    public void setTotalCancelled(int totalCancelled) {
        this.totalCancelled = totalCancelled;
    }

    public int getTotalReturned() {
        return totalReturned;
    }

    public void setTotalReturned(int totalReturned) {
        this.totalReturned = totalReturned;
    }

    public int getEfficiencyScore() {
        return efficiencyScore;
    }

    public void setEfficiencyScore(int efficiencyScore) {
        this.efficiencyScore = efficiencyScore;
    }
}
