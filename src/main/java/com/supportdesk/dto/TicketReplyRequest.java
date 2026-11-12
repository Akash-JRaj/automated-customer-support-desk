package com.supportdesk.dto;

import jakarta.validation.constraints.NotBlank;

public class TicketReplyRequest {

    @NotBlank(message = "Reply message cannot be empty")
    private String message;

    private boolean resolveTicket = false;

    public TicketReplyRequest() {
    }

    public TicketReplyRequest(String message, boolean resolveTicket) {
        this.message = message;
        this.resolveTicket = resolveTicket;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isResolveTicket() {
        return resolveTicket;
    }

    public void setResolveTicket(boolean resolveTicket) {
        this.resolveTicket = resolveTicket;
    }
}
