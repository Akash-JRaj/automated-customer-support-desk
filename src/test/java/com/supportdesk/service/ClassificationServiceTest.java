package com.supportdesk.service;

import com.supportdesk.dto.ClassificationResult;
import com.supportdesk.model.enums.Category;
import com.supportdesk.model.enums.HandlingType;
import com.supportdesk.model.enums.Urgency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class ClassificationServiceTest {

    private ClassificationService classificationService;

    @BeforeEach
    void setUp() {
        classificationService = new ClassificationService();
        ReflectionTestUtils.setField(classificationService, "autonomyThreshold", 0.75);
    }

    @Test
    @DisplayName("Should classify critical outage as CRITICAL urgency and HUMAN_ESCALATED")
    void testCriticalOutageClassification() {
        String subject = "URGENT: Production down with 500 outage across cluster";
        String body = "Our core payment API is broken and completely down. Emergency disaster!";

        ClassificationResult result = classificationService.classify(subject, body, "DevOps Engineer");

        assertThat(result.getUrgency()).isEqualTo(Urgency.CRITICAL);
        assertThat(result.getCategory()).isEqualTo(Category.TECHNICAL_SUPPORT);
        assertThat(result.getHandlingType()).isEqualTo(HandlingType.HUMAN_ESCALATED);
        assertThat(result.isRequiresEscalation()).isTrue();
        assertThat(result.getEscalationReason()).contains("Critical severity event");
        assertThat(result.getSuggestedReply()).isNotEmpty();
    }

    @Test
    @DisplayName("Should classify password reset inquiry as ACCOUNT_ACCESS and AI_HANDLED")
    void testPasswordResetAiHandled() {
        String subject = "Forgot password to sign in";
        String body = "Hello, can you help me reset my account password? I am locked out.";

        ClassificationResult result = classificationService.classify(subject, body, "Alice");

        assertThat(result.getCategory()).isEqualTo(Category.ACCOUNT_ACCESS);
        assertThat(result.getUrgency()).isNotEqualTo(Urgency.CRITICAL);
        assertThat(result.getHandlingType()).isEqualTo(HandlingType.AI_HANDLED);
        assertThat(result.isRequiresEscalation()).isFalse();
        assertThat(result.getSuggestedReply()).contains("https://app.supportdesk.io/reset-password");
    }

    @Test
    @DisplayName("Should escalate ticket when angry/frustrated sentiment is detected")
    void testFrustratedCustomerEscalation() {
        String subject = "Charged twice on invoice!";
        String body = "This is unacceptable and terrible! I am furious and demand a manager immediately!";

        ClassificationResult result = classificationService.classify(subject, body, "Angry User");

        assertThat(result.getCategory()).isEqualTo(Category.BILLING);
        assertThat(result.getSentiment()).isEqualTo("FRUSTRATED");
        assertThat(result.getHandlingType()).isEqualTo(HandlingType.HUMAN_ESCALATED);
        assertThat(result.getEscalationReason()).contains("frustration/dissatisfaction");
    }

    @Test
    @DisplayName("Should escalate security issues according to compliance policy")
    void testSecurityVulnerabilityEscalation() {
        String subject = "Security vulnerability disclosure";
        String body = "We discovered an unauthorized exploit breach vulnerability in the auth endpoint.";

        ClassificationResult result = classificationService.classify(subject, body, "Security Researcher");

        assertThat(result.getCategory()).isEqualTo(Category.SECURITY);
        assertThat(result.getHandlingType()).isEqualTo(HandlingType.HUMAN_ESCALATED);
        assertThat(result.getEscalationReason()).contains("Security concern flagged");
    }
}
