package com.supportdesk.model.enums;

public enum Urgency {
    CRITICAL(1, "Critical - Production Outage / Immediate SLA"),
    HIGH(2, "High - Degraded Core Functionality"),
    MEDIUM(3, "Medium - Standard Operational Issue"),
    LOW(4, "Low - General Query / Minor Cosmetic");

    private final int priorityWeight;
    private final String description;

    Urgency(int priorityWeight, String description) {
        this.priorityWeight = priorityWeight;
        this.description = description;
    }

    public int getPriorityWeight() {
        return priorityWeight;
    }

    public String getDescription() {
        return description;
    }
}
