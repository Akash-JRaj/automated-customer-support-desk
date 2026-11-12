package com.supportdesk.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:support@supportdesk.io}")
    private String fromEmail;

    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    public EmailNotificationService(@Autowired(required = false) JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public boolean sendEmail(String to, String subject, String body) {
        if (!mailEnabled) {
            log.info("[MOCK MAIL DISPATCH] To: {} | Subject: {} | Body length: {}", to, subject, body.length());
            return true;
        }

        if (mailSender == null) {
            log.warn("JavaMailSender not initialized. Simulating email dispatch to: {}", to);
            return true;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email dispatched successfully to {}", to);
            return true;
        } catch (Exception ex) {
            log.warn("Failed to dispatch physical email via SMTP to {} (error: {}). Simulation recorded.",
                    to, ex.getMessage());
            return false;
        }
    }
}
