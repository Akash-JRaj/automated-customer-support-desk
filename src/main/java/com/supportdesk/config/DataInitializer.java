package com.supportdesk.config;

import com.supportdesk.dto.EmailIngestionRequest;
import com.supportdesk.model.User;
import com.supportdesk.model.enums.Role;
import com.supportdesk.model.enums.TicketStatus;
import com.supportdesk.repository.TicketRepository;
import com.supportdesk.repository.UserRepository;
import com.supportdesk.service.EmailIngestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;
    private final EmailIngestionService emailIngestionService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           TicketRepository ticketRepository,
                           EmailIngestionService emailIngestionService,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
        this.emailIngestionService = emailIngestionService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        log.info("Bootstrapping Support Desk pre-seeded users and sample tickets...");

        // 1. Seed Users
        User admin = new User("admin@supportdesk.io", passwordEncoder.encode("Admin@123"), "Akash Jayaraj (Admin)", Role.ROLE_ADMIN);
        User agentSarah = new User("agent.sarah@supportdesk.io", passwordEncoder.encode("Agent@123"), "Sarah Chen (Tier-2)", Role.ROLE_AGENT);
        User agentMarcus = new User("agent.marcus@supportdesk.io", passwordEncoder.encode("Agent@123"), "Marcus Vance (Support Lead)", Role.ROLE_AGENT);
        User customerDavid = new User("david@acmecorp.com", passwordEncoder.encode("Customer@123"), "David Miller", Role.ROLE_CUSTOMER);

        userRepository.save(admin);
        userRepository.save(agentSarah);
        userRepository.save(agentMarcus);
        userRepository.save(customerDavid);

        // 2. Ingest Sample Customer Inbound Emails
        // Sample 1: Critical Server Outage -> Human Escalated
        emailIngestionService.ingestEmail(new EmailIngestionRequest(
                "devops@fintechcorp.com",
                "Alex Rivera",
                "URGENT: Production API down with 500 error outage across US-East",
                "Our core payment processing webhook is failing immediately with 500 internal server error. All client transactions are blocked! This is an emergency production incident."
        ));

        // Sample 2: Password Reset -> AI Handled & Auto-resolved
        var ticket2 = emailIngestionService.ingestEmail(new EmailIngestionRequest(
                "elena@designstudio.io",
                "Elena Rostova",
                "Need help resetting my account password and 2FA credentials",
                "Hello, I was locked out of my team account after getting a new phone. How can I reset my password and reset my two-factor authentication?"
        ));
        ticketRepository.findById(ticket2.getId()).ifPresent(t -> {
            t.setStatus(TicketStatus.RESOLVED);
            ticketRepository.save(t);
        });

        // Sample 3: Frustrated Customer -> Human Escalated
        emailIngestionService.ingestEmail(new EmailIngestionRequest(
                "mark.b@retailhub.net",
                "Mark Bradley",
                "Charged twice on monthly subscription! Unacceptable service",
                "I was charged twice on my credit card invoice for my plan renewal this morning. This is completely unacceptable and ridiculous! I demand an immediate refund and to speak with a manager right now."
        ));

        // Sample 4: General Inquiry -> AI Handled
        emailIngestionService.ingestEmail(new EmailIngestionRequest(
                "claire@startuplab.co",
                "Claire Watson",
                "Question regarding API rate limits on developer plan",
                "Hi team, where can I find the official documentation for the REST API rate limits and webhook retry backoff policy? Thanks!"
        ));

        // Sample 5: Security Vulnerability -> Human Escalated
        emailIngestionService.ingestEmail(new EmailIngestionRequest(
                "security-researcher@whitehat.org",
                "Dr. Liam Vance",
                "Potential security vulnerability disclosure in session token expiration",
                "We observed a security issue where expired JWT tokens may have a grace window during cluster synchronization. Please route to your security operations officer."
        ));

        // Sample 6: Invoice Request -> AI Handled & Resolved
        var ticket6 = emailIngestionService.ingestEmail(new EmailIngestionRequest(
                "billing@enterprisegroup.com",
                "Jessica Alba",
                "Requesting official VAT invoice for October payment",
                "Could you please send over the latest VAT tax invoice for our enterprise subscription plan for our tax records? Receipt reference #88492."
        ));
        ticketRepository.findById(ticket6.getId()).ifPresent(t -> {
            t.setStatus(TicketStatus.RESOLVED);
            ticketRepository.save(t);
        });

        log.info("Bootstrapping complete! Sample tickets and metrics ready.");
    }
}
