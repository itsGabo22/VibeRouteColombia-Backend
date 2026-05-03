package com.routeoptimizer.dto;

import java.util.List;

public class SmartDispatchPlanDTO {
    private String aiReport;
    private List<ClusterDTO> primaryClusters;
    private List<ClusterDTO> alternativeClusters;

    public SmartDispatchPlanDTO() {}

    public String getAiReport() {
        return aiReport;
    }

    public void setAiReport(String aiReport) {
        this.aiReport = aiReport;
    }

    public List<ClusterDTO> getPrimaryClusters() {
        return primaryClusters;
    }

    public void setPrimaryClusters(List<ClusterDTO> primaryClusters) {
        this.primaryClusters = primaryClusters;
    }

    public List<ClusterDTO> getAlternativeClusters() {
        return alternativeClusters;
    }

    public void setAlternativeClusters(List<ClusterDTO> alternativeClusters) {
        this.alternativeClusters = alternativeClusters;
    }
}
