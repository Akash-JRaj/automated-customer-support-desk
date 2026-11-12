package com.supportdesk.dto;

import com.supportdesk.model.enums.Category;
import com.supportdesk.model.enums.HandlingType;
import com.supportdesk.model.enums.Urgency;
import java.util.ArrayList;
import java.util.List;

public class ClassificationResult {

    private Category category;
    private Urgency urgency;
    private Double confidenceScore;
    private String suggestedReply;
    private boolean requiresEscalation;
    private String escalationReason;
    private List<String> detectedKeywords = new ArrayList<>();
    private String sentiment; // POSITIVE, NEUTRAL, FRUSTRATED, ANGRY
    private HandlingType handlingType;

    public ClassificationResult() {
    }

    public ClassificationResult(Category category, Urgency urgency, Double confidenceScore,
                                String suggestedReply, boolean requiresEscalation, String escalationReason,
                                List<String> detectedKeywords, String sentiment, HandlingType handlingType) {
        this.category = category;
        this.urgency = urgency;
        this.confidenceScore = confidenceScore;
        this.suggestedReply = suggestedReply;
        this.requiresEscalation = requiresEscalation;
        this.escalationReason = escalationReason;
        this.detectedKeywords = detectedKeywords != null ? detectedKeywords : new ArrayList<>();
        this.sentiment = sentiment;
        this.handlingType = handlingType;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Urgency getUrgency() {
        return urgency;
    }

    public void setUrgency(Urgency urgency) {
        this.urgency = urgency;
    }

    public Double getConfidenceScore() {
        return confidenceScore;
    }

    public void setConfidenceScore(Double confidenceScore) {
        this.confidenceScore = confidenceScore;
    }

    public String getSuggestedReply() {
        return suggestedReply;
    }

    public void setSuggestedReply(String suggestedReply) {
        this.suggestedReply = suggestedReply;
    }

    public boolean isRequiresEscalation() {
        return requiresEscalation;
    }

    public void setRequiresEscalation(boolean requiresEscalation) {
        this.requiresEscalation = requiresEscalation;
    }

    public String getEscalationReason() {
        return escalationReason;
    }

    public void setEscalationReason(String escalationReason) {
        this.escalationReason = escalationReason;
    }

    public List<String> getDetectedKeywords() {
        return detectedKeywords;
    }

    public void setDetectedKeywords(List<String> detectedKeywords) {
        this.detectedKeywords = detectedKeywords;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public HandlingType getHandlingType() {
        return handlingType;
    }

    public void setHandlingType(HandlingType handlingType) {
        this.handlingType = handlingType;
    }
}
