package com.supportdesk.service;

import com.supportdesk.dto.TicketDetailDto;
import com.supportdesk.dto.TicketMessageDto;
import com.supportdesk.dto.TicketResponseDto;
import com.supportdesk.model.Ticket;
import com.supportdesk.model.TicketMessage;
import com.supportdesk.model.User;
import com.supportdesk.model.enums.Category;
import com.supportdesk.model.enums.HandlingType;
import com.supportdesk.model.enums.SenderType;
import com.supportdesk.model.enums.TicketStatus;
import com.supportdesk.model.enums.Urgency;
import com.supportdesk.repository.TicketMessageRepository;
import com.supportdesk.repository.TicketRepository;
import com.supportdesk.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class TicketService {

    private static final Logger log = LoggerFactory.getLogger(TicketService.class);

    private final TicketRepository ticketRepository;
    private final TicketMessageRepository ticketMessageRepository;
    private final UserRepository userRepository;
    private final EmailNotificationService notificationService;
    private final EmailIngestionService emailIngestionService;

    public TicketService(TicketRepository ticketRepository,
                         TicketMessageRepository ticketMessageRepository,
                         UserRepository userRepository,
                         EmailNotificationService notificationService,
                         EmailIngestionService emailIngestionService) {
        this.ticketRepository = ticketRepository;
        this.ticketMessageRepository = ticketMessageRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.emailIngestionService = emailIngestionService;
    }

    @Transactional(readOnly = true)
    public Page<TicketResponseDto> getTicketsWithFilters(TicketStatus status,
                                                        Urgency urgency,
                                                        Category category,
                                                        HandlingType handlingType,
                                                        String search,
                                                        Pageable pageable) {
        Page<Ticket> page = ticketRepository.findWithFilters(status, urgency, category, handlingType, search, pageable);
        return page.map(emailIngestionService::mapToDto);
    }

    @Transactional(readOnly = true)
    public TicketDetailDto getTicketDetails(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NoSuchElementException("Ticket not found with id: " + ticketId));

        TicketDetailDto detailDto = new TicketDetailDto();
        TicketResponseDto base = emailIngestionService.mapToDto(ticket);
        copyBaseProperties(base, detailDto);

        detailDto.setDraftReply(ticket.getDraftReply());

        List<TicketMessage> messages = ticketMessageRepository.findByTicketOrderBySentAtAsc(ticket);
        List<TicketMessageDto> messageDtos = messages.stream().map(m -> new TicketMessageDto(
                m.getId(),
                m.getSenderEmail(),
                m.getSenderName(),
                m.getSenderType(),
                m.getBody(),
                m.isAutomatedReply(),
                m.getSentAt()
        )).toList();
        detailDto.setMessages(messageDtos);

        return detailDto;
    }

    @Transactional
    public TicketResponseDto updateTicketStatus(Long ticketId, TicketStatus newStatus) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NoSuchElementException("Ticket not found with id: " + ticketId));

        ticket.setStatus(newStatus);
        Ticket saved = ticketRepository.save(ticket);
        log.info("Ticket {} status updated to {}", ticket.getTicketNumber(), newStatus);
        return emailIngestionService.mapToDto(saved);
    }

    @Transactional
    public TicketResponseDto assignAgent(Long ticketId, Long agentId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NoSuchElementException("Ticket not found with id: " + ticketId));

        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new NoSuchElementException("Agent not found with id: " + agentId));

        ticket.setAssignedAgent(agent);
        if (ticket.getStatus() == TicketStatus.NEW) {
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }

        Ticket saved = ticketRepository.save(ticket);
        log.info("Ticket {} assigned to agent {}", ticket.getTicketNumber(), agent.getFullName());
        return emailIngestionService.mapToDto(saved);
    }

    @Transactional
    public TicketResponseDto escalateTicket(Long ticketId, String escalationReason) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NoSuchElementException("Ticket not found with id: " + ticketId));

        ticket.setHandlingType(HandlingType.HUMAN_ESCALATED);
        ticket.setEscalationReason(escalationReason);
        if (ticket.getUrgency() == Urgency.LOW || ticket.getUrgency() == Urgency.MEDIUM) {
            ticket.setUrgency(Urgency.HIGH);
        }

        Ticket saved = ticketRepository.save(ticket);
        log.info("Ticket {} manually escalated: {}", ticket.getTicketNumber(), escalationReason);
        return emailIngestionService.mapToDto(saved);
    }

    @Transactional
    public TicketDetailDto addAgentReply(Long ticketId, String replyBody, boolean resolveTicket, String agentEmail) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NoSuchElementException("Ticket not found with id: " + ticketId));

        User agent = (agentEmail != null) ? userRepository.findByEmail(agentEmail).orElse(null) : null;
        String agentName = (agent != null) ? agent.getFullName() : "Customer Support Agent";

        TicketMessage reply = new TicketMessage(
                agentEmail != null ? agentEmail : "support@supportdesk.io",
                agentName,
                SenderType.AGENT,
                replyBody,
                false
        );
        ticket.addMessage(reply);

        if (resolveTicket) {
            ticket.setStatus(TicketStatus.RESOLVED);
        } else {
            ticket.setStatus(TicketStatus.WAITING_ON_CUSTOMER);
        }

        // Send email out to customer
        notificationService.sendEmail(
                ticket.getCustomerEmail(),
                "Re: [" + ticket.getTicketNumber() + "] " + ticket.getSubject(),
                replyBody
        );

        ticketRepository.save(ticket);
        return getTicketDetails(ticketId);
    }

    @Transactional
    public TicketDetailDto approveDraftReply(Long ticketId, String agentEmail) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new NoSuchElementException("Ticket not found with id: " + ticketId));

        if (ticket.getDraftReply() == null || ticket.getDraftReply().isBlank()) {
            throw new IllegalStateException("No AI draft reply exists for this ticket");
        }

        return addAgentReply(ticketId, ticket.getDraftReply(), true, agentEmail);
    }

    private void copyBaseProperties(TicketResponseDto src, TicketDetailDto dest) {
        dest.setId(src.getId());
        dest.setTicketNumber(src.getTicketNumber());
        dest.setSubject(src.getSubject());
        dest.setCustomerEmail(src.getCustomerEmail());
        dest.setCustomerName(src.getCustomerName());
        dest.setCategory(src.getCategory());
        dest.setUrgency(src.getUrgency());
        dest.setStatus(src.getStatus());
        dest.setHandlingType(src.getHandlingType());
        dest.setAssignedAgentName(src.getAssignedAgentName());
        dest.setAiConfidenceScore(src.getAiConfidenceScore());
        dest.setEscalationReason(src.getEscalationReason());
        dest.setCreatedAt(src.getCreatedAt());
        dest.setUpdatedAt(src.getUpdatedAt());
        dest.setResolvedAt(src.getResolvedAt());
    }
}
