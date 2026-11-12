package com.supportdesk.controller;

import com.supportdesk.dto.EmailIngestionRequest;
import com.supportdesk.dto.TicketResponseDto;
import com.supportdesk.service.EmailIngestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/emails")
@Tag(name = "Email Ingestion & Webhooks", description = "Microservice ingestion endpoints for inbound customer emails")
public class EmailWebhookController {

    private final EmailIngestionService emailIngestionService;

    public EmailWebhookController(EmailIngestionService emailIngestionService) {
        this.emailIngestionService = emailIngestionService;
    }

    @PostMapping("/ingest")
    @Operation(summary = "Ingest an incoming email from email gateway or webhook")
    public ResponseEntity<TicketResponseDto> ingestEmail(@Valid @RequestBody EmailIngestionRequest request) {
        TicketResponseDto response = emailIngestionService.ingestEmail(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/simulate")
    @Operation(summary = "Simulate inbound email processing for dashboard testing")
    public ResponseEntity<TicketResponseDto> simulateEmail(@Valid @RequestBody EmailIngestionRequest request) {
        TicketResponseDto response = emailIngestionService.ingestEmail(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
