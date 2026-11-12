package com.supportdesk.service;

import com.supportdesk.dto.ClassificationResult;
import com.supportdesk.dto.EmailIngestionRequest;
import com.supportdesk.dto.TicketResponseDto;
import com.supportdesk.model.Ticket;
import com.supportdesk.model.TicketMessage;
import com.supportdesk.model.enums.HandlingType;
import com.supportdesk.model.enums.SenderType;
import com.supportdesk.model.enums.TicketStatus;
import com.supportdesk.repository.TicketMessageRepository;
import com.supportdesk.repository.TicketRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailIngestionService {

    private static final Logger log = LoggerFactory.getLogger(EmailIngestionService.class);
    private static final Pattern TICKET_NUM_PATTERN = Pattern.compile("TICK-\\d{6}-[A-Z0-9]{4}");

    private final TicketRepository ticketRepository;
    private final TicketMessageRepository ticketMessageRepository;
    private final ClassificationService classificationService;
    private final EmailNotificationService notificationService;

    public EmailIngestionService(TicketRepository ticketRepository,
                                 TicketMessageRepository ticketMessageRepository,
                                 ClassificationService classificationService,
                                 EmailNotificationService notificationService) {
        this.ticketRepository = ticketRepository;
        this.ticketMessageRepository = ticketMessageRepository;
        this.classificationService = classificationService;
        this.notificationService = notificationService;
    }

    @Transactional
    public TicketResponseDto ingestEmail(EmailIngestionRequest request) {
        log.info("Ingesting incoming email from: {} | Subject: {}", request.getSenderEmail(), request.getSubject());

        // Check if incoming email is a reply to an existing ticket
        Optional<Ticket> existingTicketOpt = findExistingTicketFromSubject(request.getSubject());
        if (existingTicketOpt.isPresent()) {
            return handleReplyToExistingTicket(existingTicketOpt.get(), request);
        }

        // New Ticket Workflow
        String customerName = resolveCustomerName(request.getSenderName(), request.getSenderEmail());
        String ticketNumber = generateTicketNumber();

        // Run Intelligent AI/Heuristic Classification
        ClassificationResult classification = classificationService.classify(
                request.getSubject(),
                request.getBody(),
                customerName
        );

        Ticket ticket = new Ticket(
                ticketNumber,
                request.getSubject(),
                request.getSenderEmail(),
                customerName,
                classification.getCategory(),
                classification.getUrgency(),
                classification.getHandlingType(),
                classification.getConfidenceScore()
        );

        ticket.setEscalationReason(classification.getEscalationReason());
        ticket.setDraftReply(classification.getSuggestedReply());

        // Initial Customer Message
        TicketMessage customerMessage = new TicketMessage(
                request.getSenderEmail(),
                customerName,
                SenderType.CUSTOMER,
                request.getBody(),
                false
        );
        ticket.addMessage(customerMessage);

        if (classification.getHandlingType() == HandlingType.AI_HANDLED) {
            ticket.setStatus(TicketStatus.WAITING_ON_CUSTOMER);

            // Automated AI response
            TicketMessage aiReplyMessage = new TicketMessage(
                    "ai-assistant@supportdesk.io",
                    "Support Desk AI Assistant",
                    SenderType.AI_SYSTEM,
                    classification.getSuggestedReply(),
                    true
            );
            ticket.addMessage(aiReplyMessage);

            // Dispatch outbound reply to customer
            notificationService.sendEmail(
                    request.getSenderEmail(),
                    "Re: [" + ticketNumber + "] " + request.getSubject(),
                    classification.getSuggestedReply()
            );
        } else {
            // Human Escalated
            ticket.setStatus(TicketStatus.NEW);

            // Send Acknowledgment & Escalation Notice
            String ackNotice = "Hello " + customerName + ",\n\n"
                    + "Your support ticket [" + ticketNumber + "] has been received and escalated to our specialist team for review.\n"
                    + "Case: " + request.getSubject() + "\n"
                    + "Priority: " + classification.getUrgency().name() + "\n\n"
                    + "A support engineer will review and reply directly to this thread shortly.\n\n"
                    + "Best regards,\nCustomer Support Operations";

            TicketMessage ackMessage = new TicketMessage(
                    "system@supportdesk.io",
                    "Support Desk System",
                    SenderType.AI_SYSTEM,
                    ackNotice,
                    true
            );
            ticket.addMessage(ackMessage);

            notificationService.sendEmail(
                    request.getSenderEmail(),
                    "Receipt: [" + ticketNumber + "] " + request.getSubject(),
                    ackNotice
            );
        }

        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Ticket created successfully: {} (Handling: {}, Urgency: {})",
                savedTicket.getTicketNumber(), savedTicket.getHandlingType(), savedTicket.getUrgency());

        return mapToDto(savedTicket);
    }

    private TicketResponseDto handleReplyToExistingTicket(Ticket ticket, EmailIngestionRequest request) {
        String customerName = resolveCustomerName(request.getSenderName(), request.getSenderEmail());

        TicketMessage reply = new TicketMessage(
                request.getSenderEmail(),
                customerName,
                SenderType.CUSTOMER,
                request.getBody(),
                false
        );
        ticket.addMessage(reply);

        if (ticket.getStatus() == TicketStatus.RESOLVED || ticket.getStatus() == TicketStatus.CLOSED) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        } else if (ticket.getStatus() == TicketStatus.WAITING_ON_CUSTOMER) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        Ticket saved = ticketRepository.save(ticket);
        log.info("Appended reply to existing ticket {}", ticket.getTicketNumber());
        return mapToDto(saved);
    }

    private Optional<Ticket> findExistingTicketFromSubject(String subject) {
        if (subject == null) return Optional.empty();
        Matcher matcher = TICKET_NUM_PATTERN.matcher(subject);
        if (matcher.find()) {
            String ticketNumber = matcher.group();
            return ticketRepository.findByTicketNumber(ticketNumber);
        }
        return Optional.empty();
    }

    private String resolveCustomerName(String senderName, String email) {
        if (senderName != null && !senderName.isBlank()) {
            return senderName.trim();
        }
        if (email != null && email.contains("@")) {
            String handle = email.substring(0, email.indexOf("@"));
            return capitalize(handle.replace(".", " ").replace("_", " "));
        }
        return "Valued Customer";
    }

    private String capitalize(String text) {
        if (text == null || text.isBlank()) return text;
        String[] words = text.split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)))
                  .append(w.substring(1).toLowerCase())
                  .append(" ");
            }
        }
        return sb.toString().trim();
    }

    private String generateTicketNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        String randomPart = UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
        return "TICK-" + datePart + "-" + randomPart;
    }

    public TicketResponseDto mapToDto(Ticket ticket) {
        TicketResponseDto dto = new TicketResponseDto();
        dto.setId(ticket.getId());
        dto.setTicketNumber(ticket.getTicketNumber());
        dto.setSubject(ticket.getSubject());
        dto.setCustomerEmail(ticket.getCustomerEmail());
        dto.setCustomerName(ticket.getCustomerName());
        dto.setCategory(ticket.getCategory());
        dto.setUrgency(ticket.getUrgency());
        dto.setStatus(ticket.getStatus());
        dto.setHandlingType(ticket.getHandlingType());
        dto.setAiConfidenceScore(ticket.getAiConfidenceScore());
        dto.setEscalationReason(ticket.getEscalationReason());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        dto.setResolvedAt(ticket.getResolvedAt());
        if (ticket.getAssignedAgent() != null) {
            dto.setAssignedAgentName(ticket.getAssignedAgent().getFullName());
        }
        return dto;
    }
}
