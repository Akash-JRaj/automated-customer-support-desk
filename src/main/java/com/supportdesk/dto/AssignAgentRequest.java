package com.supportdesk.dto;

import jakarta.validation.constraints.NotNull;

public class AssignAgentRequest {

    @NotNull(message = "Agent ID cannot be null")
    private Long agentId;

    public AssignAgentRequest() {
    }

    public AssignAgentRequest(Long agentId) {
        this.agentId = agentId;
    }

    public Long getAgentId() {
        return agentId;
    }

    public void setAgentId(Long agentId) {
        this.agentId = agentId;
    }
}
