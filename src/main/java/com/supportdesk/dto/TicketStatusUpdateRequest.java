package com.supportdesk.dto;

import com.supportdesk.model.enums.TicketStatus;
import jakarta.validation.constraints.NotNull;

public class TicketStatusUpdateRequest {

    @NotNull(message = "Status cannot be null")
    private TicketStatus status;

    public TicketStatusUpdateRequest() {
    }

    public TicketStatusUpdateRequest(TicketStatus status) {
        this.status = status;
    }

    public TicketStatus getStatus() {
        return status;
    }

    public void setStatus(TicketStatus status) {
        this.status = status;
    }
}
