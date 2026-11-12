package com.supportdesk.controller;

import com.supportdesk.dto.*;
import com.supportdesk.model.enums.Category;
import com.supportdesk.model.enums.HandlingType;
import com.supportdesk.model.enums.TicketStatus;
import com.supportdesk.model.enums.Urgency;
import com.supportdesk.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tickets")
@Tag(name = "Tickets Management", description = "Operations for viewing, updating, and replying to customer tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    @Operation(summary = "Search and filter tickets with pagination")
    public ResponseEntity<Page<TicketResponseDto>> getTickets(
            @RequestParam(required = false) TicketStatus status,
            @RequestParam(required = false) Urgency urgency,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) HandlingType handlingType,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<TicketResponseDto> result = ticketService.getTicketsWithFilters(status, urgency, category, handlingType, search, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get full ticket details and message thread by ID")
    public ResponseEntity<TicketDetailDto> getTicketDetails(@PathVariable Long id) {
        return ResponseEntity.ok(ticketService.getTicketDetails(id));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update ticket resolution status")
    public ResponseEntity<TicketResponseDto> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody TicketStatusUpdateRequest request
    ) {
        return ResponseEntity.ok(ticketService.updateTicketStatus(id, request.getStatus()));
    }

    @PostMapping("/{id}/assign")
    @Operation(summary = "Assign ticket to support agent")
    public ResponseEntity<TicketResponseDto> assignAgent(
            @PathVariable Long id,
            @Valid @RequestBody AssignAgentRequest request
    ) {
        return ResponseEntity.ok(ticketService.assignAgent(id, request.getAgentId()));
    }

    @PostMapping("/{id}/escalate")
    @Operation(summary = "Manually escalate ticket to human engineering / specialist")
    public ResponseEntity<TicketResponseDto> escalateTicket(
            @PathVariable Long id,
            @RequestParam(defaultValue = "Manually escalated by support agent") String reason
    ) {
        return ResponseEntity.ok(ticketService.escalateTicket(id, reason));
    }

    @PostMapping("/{id}/reply")
    @Operation(summary = "Send agent reply to customer")
    public ResponseEntity<TicketDetailDto> addReply(
            @PathVariable Long id,
            @Valid @RequestBody TicketReplyRequest request,
            Authentication authentication
    ) {
        String agentEmail = (authentication != null) ? authentication.getName() : null;
        return ResponseEntity.ok(ticketService.addAgentReply(id, request.getMessage(), request.isResolveTicket(), agentEmail));
    }

    @PostMapping("/{id}/approve-draft")
    @Operation(summary = "Approve and send AI draft reply directly to customer")
    public ResponseEntity<TicketDetailDto> approveDraftReply(
            @PathVariable Long id,
            Authentication authentication
    ) {
        String agentEmail = (authentication != null) ? authentication.getName() : null;
        return ResponseEntity.ok(ticketService.approveDraftReply(id, agentEmail));
    }
}
