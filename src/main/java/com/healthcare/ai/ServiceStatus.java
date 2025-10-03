package com.healthcare.ai;

public class ServiceStatus {
    private final boolean enabled;
    private final String name;
    private final boolean healthy;
    private final String status;

    public ServiceStatus(boolean enabled, String name, boolean healthy) {
        this.enabled = enabled;
        this.name = name;
        this.healthy = healthy;
        this.status = healthy ? "HEALTHY" : (enabled ? "UNHEALTHY" : "DISABLED");
    }

    // Add default constructor for JSON serialization
    public ServiceStatus() {
        this.enabled = false;
        this.name = "Unknown";
        this.healthy = false;
        this.status = "UNKNOWN";
    }

    // Getters (required for JSON serialization)
    public boolean isEnabled() { return enabled; }
    public String getName() { return name; }
    public boolean isHealthy() { return healthy; }
    public String getStatus() { return status; }
}