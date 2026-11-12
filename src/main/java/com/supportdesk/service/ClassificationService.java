package com.supportdesk.service;

import com.supportdesk.dto.ClassificationResult;
import com.supportdesk.model.enums.Category;
import com.supportdesk.model.enums.HandlingType;
import com.supportdesk.model.enums.Urgency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Pattern;

@Service
public class ClassificationService {

    private static final Logger log = LoggerFactory.getLogger(ClassificationService.class);

    @Value("${app.ai.autonomy-threshold:0.75}")
    private double autonomyThreshold;

    private static final Map<Category, Set<String>> CATEGORY_KEYWORDS = new EnumMap<>(Category.class);
    private static final Map<Urgency, Set<String>> URGENCY_KEYWORDS = new EnumMap<>(Urgency.class);
    private static final Set<String> FRUSTRATION_KEYWORDS = new HashSet<>();

    static {
        // Billing Keywords
        CATEGORY_KEYWORDS.put(Category.BILLING, Set.of(
                "billing", "invoice", "refund", "charge", "charged", "overcharged", "subscription",
                "payment", "credit card", "receipt", "plan", "pricing", "stripe", "renewal", "cancel subscription"
        ));

        // Technical Support Keywords
        CATEGORY_KEYWORDS.put(Category.TECHNICAL_SUPPORT, Set.of(
                "bug", "error", "crash", "exception", "failed", "500", "404", "stacktrace",
                "broken", "timeout", "latency", "connection", "api", "database", "issue", "not working", "glitch"
        ));

        // Account Access Keywords
        CATEGORY_KEYWORDS.put(Category.ACCOUNT_ACCESS, Set.of(
                "password", "login", "reset", "2fa", "mfa", "locked", "unlock", "credentials",
                "otp", "authentication", "sso", "sign in", "access denied", "email verification"
        ));

        // Security Keywords
        CATEGORY_KEYWORDS.put(Category.SECURITY, Set.of(
                "security", "vulnerability", "breach", "hack", "hacked", "phishing", "exploit",
                "unauthorized", "leaked", "cve", "compromised", "malware"
        ));

        // Feature Request Keywords
        CATEGORY_KEYWORDS.put(Category.FEATURE_REQUEST, Set.of(
                "feature request", "feature", "suggestion", "enhancement", "roadmap", "wishlist",
                "would be great", "can you add", "support for", "integrate"
        ));

        // General Inquiry Keywords
        CATEGORY_KEYWORDS.put(Category.GENERAL_INQUIRY, Set.of(
                "hours", "contact", "sales", "demo", "info", "information", "question",
                "documentation", "how does", "pricing inquiry", "partnership"
        ));

        // Urgency: Critical
        URGENCY_KEYWORDS.put(Urgency.CRITICAL, Set.of(
                "outage", "production down", "emergency", "catastrophic", "data loss",
                "sev-1", "severity 1", "system completely down", "critical security", "breached"
        ));

        // Urgency: High
        URGENCY_KEYWORDS.put(Urgency.HIGH, Set.of(
                "urgent", "asap", "blocking", "blocked", "cannot access", "immediately",
                "production", "time sensitive", "high priority", "escalate", "customer impacted"
        ));

        // Urgency: Medium
        URGENCY_KEYWORDS.put(Urgency.MEDIUM, Set.of(
                "error", "failed", "bug", "payment failed", "wrong invoice", "problem", "assistance"
        ));

        // Urgency: Low
        URGENCY_KEYWORDS.put(Urgency.LOW, Set.of(
                "question", "feedback", "suggestion", "minor", "typo", "wondering", "curious"
        ));

        // Frustration / Escalation trigger keywords
        FRUSTRATION_KEYWORDS.addAll(Set.of(
                "furious", "angry", "unacceptable", "terrible", "worst", "lawsuit", "sue",
                "incompetent", "cancel immediately", "disaster", "waste of money", "demand a manager", "ridiculous"
        ));
    }

    public ClassificationResult classify(String subject, String body, String customerName) {
        String combinedText = ((subject != null ? subject : "") + " " + (body != null ? body : "")).toLowerCase();
        List<String> matchedKeywords = new ArrayList<>();

        // 1. Detect Category
        Map<Category, Integer> categoryScores = new EnumMap<>(Category.class);
        for (Map.Entry<Category, Set<String>> entry : CATEGORY_KEYWORDS.entrySet()) {
            int score = 0;
            for (String kw : entry.getValue()) {
                if (containsWordOrPhrase(combinedText, kw)) {
                    score += kw.contains(" ") ? 3 : 1; // multi-word matches carry higher weight
                    matchedKeywords.add(kw);
                }
            }
            categoryScores.put(entry.getKey(), score);
        }

        Category bestCategory = categoryScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .filter(e -> e.getValue() > 0)
                .map(Map.Entry::getKey)
                .orElse(Category.GENERAL_INQUIRY);

        // 2. Detect Urgency
        Urgency detectedUrgency = Urgency.LOW;
        if (hasAnyKeyword(combinedText, URGENCY_KEYWORDS.get(Urgency.CRITICAL), matchedKeywords)) {
            detectedUrgency = Urgency.CRITICAL;
        } else if (hasAnyKeyword(combinedText, URGENCY_KEYWORDS.get(Urgency.HIGH), matchedKeywords)) {
            detectedUrgency = Urgency.HIGH;
        } else if (hasAnyKeyword(combinedText, URGENCY_KEYWORDS.get(Urgency.MEDIUM), matchedKeywords)) {
            detectedUrgency = Urgency.MEDIUM;
        }

        // 3. Sentiment Analysis
        boolean isFrustrated = hasAnyKeyword(combinedText, FRUSTRATION_KEYWORDS, matchedKeywords);
        String sentiment = isFrustrated ? "FRUSTRATED" : (detectedUrgency == Urgency.CRITICAL ? "URGENT" : "NEUTRAL");

        // 4. Confidence Score Calculation
        int matchCount = matchedKeywords.size();
        double baseConfidence = 0.65;
        if (matchCount >= 4) {
            baseConfidence = 0.95;
        } else if (matchCount >= 2) {
            baseConfidence = 0.88;
        } else if (matchCount == 1) {
            baseConfidence = 0.75;
        }
        double confidenceScore = Math.min(0.99, baseConfidence);

        // 5. Handling Decision (AI vs Human Escalation)
        boolean requiresEscalation = false;
        String escalationReason = null;

        if (detectedUrgency == Urgency.CRITICAL) {
            requiresEscalation = true;
            escalationReason = "Critical severity event detected (potential production disruption / SLA urgency). Human lead intervention required.";
        } else if (isFrustrated) {
            requiresEscalation = true;
            escalationReason = "High frustration/dissatisfaction sentiment detected. Routing to human specialist for empathetic resolution.";
        } else if (bestCategory == Category.SECURITY) {
            requiresEscalation = true;
            escalationReason = "Security concern flagged. Compliance policy dictates human security officer review.";
        } else if (confidenceScore < autonomyThreshold) {
            requiresEscalation = true;
            escalationReason = String.format("AI classification confidence (%.1f%%) below autonomy threshold (%.1f%%). Routing for human triage.",
                    confidenceScore * 100, autonomyThreshold * 100);
        }

        HandlingType handlingType = requiresEscalation ? HandlingType.HUMAN_ESCALATED : HandlingType.AI_HANDLED;

        // 6. Draft Contextual Reply
        String suggestedReply = generateDraftReply(bestCategory, detectedUrgency, handlingType, customerName, subject);

        log.info("Classified ticket | Category: {} | Urgency: {} | Handling: {} | Confidence: {}",
                bestCategory, detectedUrgency, handlingType, confidenceScore);

        return new ClassificationResult(
                bestCategory,
                detectedUrgency,
                confidenceScore,
                suggestedReply,
                requiresEscalation,
                escalationReason,
                matchedKeywords,
                sentiment,
                handlingType
        );
    }

    private boolean containsWordOrPhrase(String text, String phrase) {
        if (phrase.contains(" ")) {
            return text.contains(phrase);
        }
        return Pattern.compile("\\b" + Pattern.quote(phrase) + "\\b").matcher(text).find();
    }

    private boolean hasAnyKeyword(String text, Set<String> keywords, List<String> matchedKeywords) {
        if (keywords == null) return false;
        boolean found = false;
        for (String kw : keywords) {
            if (containsWordOrPhrase(text, kw)) {
                matchedKeywords.add(kw);
                found = true;
            }
        }
        return found;
    }

    private String generateDraftReply(Category category, Urgency urgency, HandlingType handlingType,
                                      String customerName, String subject) {
        String greeting = (customerName != null && !customerName.isBlank()) ? "Hello " + customerName + "," : "Hello,";

        if (handlingType == HandlingType.HUMAN_ESCALATED) {
            return greeting + "\n\n"
                    + "Thank you for reaching out to customer support regarding: \"" + (subject != null ? subject : "your inquiry") + "\".\n\n"
                    + "Because this matter involves " + category.getDisplayName().toLowerCase() + " with "
                    + urgency.name().toLowerCase() + " priority, your ticket has been expedited directly to our senior specialist support team.\n\n"
                    + "A dedicated support engineer is actively reviewing your case details and will follow up with you directly within our guaranteed response SLA.\n\n"
                    + "Best regards,\nCustomer Support Operations Team";
        }

        return switch (category) {
            case BILLING -> greeting + "\n\n"
                    + "Thank you for contacting our Billing Department. We have received your query regarding your billing statement.\n\n"
                    + "• If you are inquiring about a refund: Refunds are automatically processed back to the original payment method within 3-5 business days.\n"
                    + "• If you need an updated VAT/GST invoice: You can download complete PDF invoices anytime from your Account Settings -> Billing & Invoices tab.\n\n"
                    + "If your issue involves an incorrect charge, please reply with the last 4 digits of the payment method and transaction date so we can issue an immediate correction.\n\n"
                    + "Best regards,\nAutomated Billing Assistant";

            case ACCOUNT_ACCESS -> greeting + "\n\n"
                    + "Thank you for contacting Support regarding your account credentials.\n\n"
                    + "To quickly restore access:\n"
                    + "1. Visit the Self-Service Password Reset page: https://app.supportdesk.io/reset-password\n"
                    + "2. Enter your registered email address to receive a secure one-time verification link (valid for 15 minutes).\n"
                    + "3. If 2FA authentication is failing, please ensure your device clock is synchronized, or use your emergency recovery keys.\n\n"
                    + "Please reply to this email if you need us to temporarily unlock your session or re-issue recovery tokens.\n\n"
                    + "Best regards,\nIdentity & Access Team";

            case TECHNICAL_SUPPORT -> greeting + "\n\n"
                    + "Thank you for reporting this technical issue. Our diagnostic telemetry has recorded your query.\n\n"
                    + "Recommended quick steps:\n"
                    + "1. Check our real-time system status page at https://status.supportdesk.io for any ongoing service degradation.\n"
                    + "2. Clear local browser cache/cookies or test API authentication headers with a fresh Bearer token.\n"
                    + "3. If the error persists, please reply with the approximate timestamp, endpoint/URL, and full error response code.\n\n"
                    + "Our automated diagnostics are scanning our application logs for your account ID.\n\n"
                    + "Best regards,\nTechnical Support Engine";

            case FEATURE_REQUEST -> greeting + "\n\n"
                    + "Thank you for sharing your feedback and feature suggestion with us!\n\n"
                    + "We love hearing ideas from our users. Your proposal has been categorized and forwarded directly to our Product Engineering team for evaluation in our upcoming quarterly roadmap review.\n\n"
                    + "You can monitor upcoming releases and vote on community requests on our public roadmap at https://feedback.supportdesk.io.\n\n"
                    + "Warm regards,\nProduct Experience Team";

            case SECURITY -> greeting + "\n\n"
                    + "Thank you for bringing this security inquiry to our attention.\n\n"
                    + "Our Information Security Office takes all security communications seriously. We have logged this report and our security operations center (SOC) is conducting an initial assessment.\n\n"
                    + "If you have additional log excerpts, headers, or reproduction steps, please reply to this thread.\n\n"
                    + "Sincerely,\nInformation Security Team";

            case GENERAL_INQUIRY -> greeting + "\n\n"
                    + "Thank you for contacting Customer Support! We are delighted to assist you.\n\n"
                    + "Our comprehensive documentation and knowledge base are available 24/7 at https://docs.supportdesk.io.\n\n"
                    + "If you have any further questions or require specific guidance, feel free to reply directly to this message.\n\n"
                    + "Warm regards,\nCustomer Support Team";
        };
    }
}
