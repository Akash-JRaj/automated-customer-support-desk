package com.supportdesk.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.supportdesk.model.enums.SenderType;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ticket_messages", indexes = {
    @Index(name = "idx_ticket_message_ticket", columnList = "ticket_id"),
    @Index(name = "idx_ticket_message_sent", columnList = "sentAt")
})
public class TicketMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    @JsonIgnore
    private Ticket ticket;

    @Column(nullable = false)
    private String senderEmail;

    @Column(nullable = false)
    private String senderName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SenderType senderType;

    @Column(nullable = false, length = 10000)
    private String body;

    @Column(nullable = false)
    private boolean isAutomatedReply = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt = LocalDateTime.now();

    public TicketMessage() {
    }

    public TicketMessage(String senderEmail, String senderName, SenderType senderType, String body, boolean isAutomatedReply) {
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.senderType = senderType;
        this.body = body;
        this.isAutomatedReply = isAutomatedReply;
        this.sentAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public SenderType getSenderType() {
        return senderType;
    }

    public void setSenderType(SenderType senderType) {
        this.senderType = senderType;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isAutomatedReply() {
        return isAutomatedReply;
    }

    public void setAutomatedReply(boolean automatedReply) {
        isAutomatedReply = automatedReply;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
