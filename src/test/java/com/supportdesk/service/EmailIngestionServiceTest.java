package com.supportdesk.service;

import com.supportdesk.dto.EmailIngestionRequest;
import com.supportdesk.dto.TicketResponseDto;
import com.supportdesk.model.Ticket;
import com.supportdesk.model.enums.Category;
import com.supportdesk.model.enums.HandlingType;
import com.supportdesk.model.enums.TicketStatus;
import com.supportdesk.model.enums.Urgency;
import com.supportdesk.repository.TicketMessageRepository;
import com.supportdesk.repository.TicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class EmailIngestionServiceTest {

    @Autowired
    private EmailIngestionService emailIngestionService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketMessageRepository messageRepository;

    @Test
    @DisplayName("Should ingest incoming email and create ticket with AI classification")
    void testIngestNewEmail() {
        EmailIngestionRequest request = new EmailIngestionRequest(
                "sarah.connor@cyberdyne.org",
                "Sarah Connor",
                "Question regarding monthly invoice and pricing receipt",
                "Could you clarify the subscription charge on our latest invoice? Need the receipt for tax filing."
        );

        TicketResponseDto response = emailIngestionService.ingestEmail(request);

        assertThat(response).isNotNull();
        assertThat(response.getTicketNumber()).startsWith("TICK-");
        assertThat(response.getCategory()).isEqualTo(Category.BILLING);
        assertThat(response.getHandlingType()).isEqualTo(HandlingType.AI_HANDLED);
        assertThat(response.getStatus()).isEqualTo(TicketStatus.WAITING_ON_CUSTOMER);

        Ticket savedTicket = ticketRepository.findById(response.getId()).orElseThrow();
        assertThat(savedTicket.getMessages()).hasSize(2); // Initial customer message + AI automated reply
        assertThat(savedTicket.getMessages().get(1).isAutomatedReply()).isTrue();
    }

    @Test
    @DisplayName("Should append message to existing ticket when incoming email has matching ticket number in subject")
    void testReplyToExistingTicket() {
        // First email
        EmailIngestionRequest first = new EmailIngestionRequest(
                "john.doe@example.com",
                "John Doe",
                "Help with technical error 500",
                "I am encountering a bug in the application."
        );
        TicketResponseDto initialTicket = emailIngestionService.ingestEmail(first);

        // Follow-up email referencing ticket number
        EmailIngestionRequest reply = new EmailIngestionRequest(
                "john.doe@example.com",
                "John Doe",
                "Re: [" + initialTicket.getTicketNumber() + "] Help with technical error 500",
                "Here is the additional error log you requested."
        );
        TicketResponseDto updatedTicket = emailIngestionService.ingestEmail(reply);

        assertThat(updatedTicket.getId()).isEqualTo(initialTicket.getId());
        Ticket reloaded = ticketRepository.findById(initialTicket.getId()).orElseThrow();
        assertThat(reloaded.getMessages()).hasSizeGreaterThanOrEqualTo(2);
    }
}
