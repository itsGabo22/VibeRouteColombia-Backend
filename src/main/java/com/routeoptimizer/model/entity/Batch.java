package com.routeoptimizer.model.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "batches")
public class Batch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid = UUID.randomUUID();

    @Column(nullable = false)
    private LocalDate creationDate;

    @Column(nullable = false)
    private String status; // OPEN, ASSIGNED, FULL_UNASSIGNED

    @Column(name = "city")
    private String city;

    @OneToMany
    @JoinColumn(name = "batch_id", insertable = false, updatable = false)
    @OrderBy("deliveryOrder ASC")
    private List<Order> orders = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(name = "manifest_url")
    private String manifestUrl;

    /**
     * AI-generated copilot tips stored as a JSON array string.
     * Generated once at driver assignment time (Cache/Memento Pattern)
     * to avoid repeated Gemini API calls during delivery.
     */
    @Column(name = "ai_copilot_tips", columnDefinition = "TEXT")
    private String aiCopilotTips;

    public Batch() {
        this.creationDate = LocalDate.now();
        this.status = "OPEN";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public String getManifestUrl() {
        return manifestUrl;
    }

    public void setManifestUrl(String manifestUrl) {
        this.manifestUrl = manifestUrl;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAiCopilotTips() {
        return aiCopilotTips;
    }

    public void setAiCopilotTips(String aiCopilotTips) {
        this.aiCopilotTips = aiCopilotTips;
    }
}
