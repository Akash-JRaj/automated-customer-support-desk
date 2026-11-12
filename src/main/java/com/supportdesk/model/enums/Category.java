package com.supportdesk.model.enums;

public enum Category {
    BILLING("Billing, Invoice & Payments"),
    TECHNICAL_SUPPORT("Technical & System Issues"),
    ACCOUNT_ACCESS("Account, Login & Credentials"),
    SECURITY("Security & Compliance Concerns"),
    FEATURE_REQUEST("Feature Request & Feedback"),
    GENERAL_INQUIRY("General Inquiries");

    private final String displayName;

    Category(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
