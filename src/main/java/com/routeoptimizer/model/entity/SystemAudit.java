package com.routeoptimizer.model.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_audits")
public class SystemAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String severity; // INFO, WARNING, CRITICAL, AUDIT

    @Column(columnDefinition = "TEXT")
    private String details;

    public SystemAudit() {}

    public SystemAudit(String userEmail, String action, String severity, String details) {
        this.timestamp = LocalDateTime.now();
        this.userEmail = userEmail;
        this.action = action;
        this.severity = severity;
        this.details = details;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
}
