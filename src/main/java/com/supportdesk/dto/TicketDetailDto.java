package com.supportdesk.dto;

import java.util.ArrayList;
import java.util.List;

public class TicketDetailDto extends TicketResponseDto {

    private String draftReply;
    private List<TicketMessageDto> messages = new ArrayList<>();

    public TicketDetailDto() {
    }

    public String getDraftReply() {
        return draftReply;
    }

    public void setDraftReply(String draftReply) {
        this.draftReply = draftReply;
    }

    public List<TicketMessageDto> getMessages() {
        return messages;
    }

    public void setMessages(List<TicketMessageDto> messages) {
        this.messages = messages;
    }
}
