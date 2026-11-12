package com.supportdesk.dto;

import com.supportdesk.model.enums.Category;
import com.supportdesk.model.enums.Urgency;
import java.util.EnumMap;
import java.util.Map;

public class DashboardMetricsDto {

    private long totalTickets;
    private long openTickets;
    private long resolvedTickets;
    private double resolutionRate; // 0.0 - 100.0
    private long aiHandledCount;
    private long humanEscalatedCount;
    private double aiAutonomyRate; // 0.0 - 100.0
    private double averageResolutionMinutes;

    private Map<Category, Long> categoryDistribution = new EnumMap<>(Category.class);
    private Map<Urgency, Long> urgencyDistribution = new EnumMap<>(Urgency.class);

    public DashboardMetricsDto() {
    }

    public long getTotalTickets() {
        return totalTickets;
    }

    public void setTotalTickets(long totalTickets) {
        this.totalTickets = totalTickets;
    }

    public long getOpenTickets() {
        return openTickets;
    }

    public void setOpenTickets(long openTickets) {
        this.openTickets = openTickets;
    }

    public long getResolvedTickets() {
        return resolvedTickets;
    }

    public void setResolvedTickets(long resolvedTickets) {
        this.resolvedTickets = resolvedTickets;
    }

    public double getResolutionRate() {
        return resolutionRate;
    }

    public void setResolutionRate(double resolutionRate) {
        this.resolutionRate = resolutionRate;
    }

    public long getAiHandledCount() {
        return aiHandledCount;
    }

    public void setAiHandledCount(long aiHandledCount) {
        this.aiHandledCount = aiHandledCount;
    }

    public long getHumanEscalatedCount() {
        return humanEscalatedCount;
    }

    public void setHumanEscalatedCount(long humanEscalatedCount) {
        this.humanEscalatedCount = humanEscalatedCount;
    }

    public double getAiAutonomyRate() {
        return aiAutonomyRate;
    }

    public void setAiAutonomyRate(double aiAutonomyRate) {
        this.aiAutonomyRate = aiAutonomyRate;
    }

    public double getAverageResolutionMinutes() {
        return averageResolutionMinutes;
    }

    public void setAverageResolutionMinutes(double averageResolutionMinutes) {
        this.averageResolutionMinutes = averageResolutionMinutes;
    }

    public Map<Category, Long> getCategoryDistribution() {
        return categoryDistribution;
    }

    public void setCategoryDistribution(Map<Category, Long> categoryDistribution) {
        this.categoryDistribution = categoryDistribution;
    }

    public Map<Urgency, Long> getUrgencyDistribution() {
        return urgencyDistribution;
    }

    public void setUrgencyDistribution(Map<Urgency, Long> urgencyDistribution) {
        this.urgencyDistribution = urgencyDistribution;
    }
}
