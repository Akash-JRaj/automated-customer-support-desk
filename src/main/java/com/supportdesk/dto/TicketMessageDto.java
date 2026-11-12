package com.supportdesk.dto;

import com.supportdesk.model.enums.SenderType;
import java.time.LocalDateTime;

public class TicketMessageDto {

    private Long id;
    private String senderEmail;
    private String senderName;
    private SenderType senderType;
    private String body;
    private boolean isAutomatedReply;
    private LocalDateTime sentAt;

    public TicketMessageDto() {
    }

    public TicketMessageDto(Long id, String senderEmail, String senderName,
                            SenderType senderType, String body, boolean isAutomatedReply, LocalDateTime sentAt) {
        this.id = id;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
        this.senderType = senderType;
        this.body = body;
        this.isAutomatedReply = isAutomatedReply;
        this.sentAt = sentAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
