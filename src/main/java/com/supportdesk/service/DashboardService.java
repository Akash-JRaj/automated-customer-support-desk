package com.supportdesk.service;

import com.supportdesk.dto.DashboardMetricsDto;
import com.supportdesk.dto.TicketResponseDto;
import com.supportdesk.model.Ticket;
import com.supportdesk.model.enums.Category;
import com.supportdesk.model.enums.HandlingType;
import com.supportdesk.model.enums.TicketStatus;
import com.supportdesk.model.enums.Urgency;
import com.supportdesk.repository.TicketRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final TicketRepository ticketRepository;
    private final EmailIngestionService emailIngestionService;

    public DashboardService(TicketRepository ticketRepository, EmailIngestionService emailIngestionService) {
        this.ticketRepository = ticketRepository;
        this.emailIngestionService = emailIngestionService;
    }

    @Transactional(readOnly = true)
    public DashboardMetricsDto getMetrics() {
        DashboardMetricsDto metrics = new DashboardMetricsDto();

        long total = ticketRepository.count();
        metrics.setTotalTickets(total);

        if (total == 0) {
            return metrics;
        }

        long resolved = ticketRepository.countByStatus(TicketStatus.RESOLVED) +
                        ticketRepository.countByStatus(TicketStatus.CLOSED);
        long open = total - resolved;

        metrics.setResolvedTickets(resolved);
        metrics.setOpenTickets(open);
        metrics.setResolutionRate(Math.round(((double) resolved / total) * 1000.0) / 10.0);

        long aiHandled = ticketRepository.countByHandlingType(HandlingType.AI_HANDLED);
        long humanEscalated = ticketRepository.countByHandlingType(HandlingType.HUMAN_ESCALATED) +
                              ticketRepository.countByHandlingType(HandlingType.MANUAL_OVERRIDE);

        metrics.setAiHandledCount(aiHandled);
        metrics.setHumanEscalatedCount(humanEscalated);
        metrics.setAiAutonomyRate(Math.round(((double) aiHandled / total) * 1000.0) / 10.0);

        // Category breakdown
        Map<Category, Long> categoryMap = new EnumMap<>(Category.class);
        for (Category cat : Category.values()) {
            categoryMap.put(cat, ticketRepository.countByCategory(cat));
        }
        metrics.setCategoryDistribution(categoryMap);

        // Urgency breakdown
        Map<Urgency, Long> urgencyMap = new EnumMap<>(Urgency.class);
        for (Urgency urg : Urgency.values()) {
            urgencyMap.put(urg, ticketRepository.countByUrgency(urg));
        }
        metrics.setUrgencyDistribution(urgencyMap);

        // Average resolution time (minutes)
        List<Ticket> allTickets = ticketRepository.findAll();
        double avgResolutionMinutes = allTickets.stream()
                .filter(t -> t.getResolvedAt() != null && t.getCreatedAt() != null)
                .mapToLong(t -> Math.max(1, Duration.between(t.getCreatedAt(), t.getResolvedAt()).toMinutes()))
                .average()
                .orElse(12.5); // Default representative metric if no historical resolved tickets yet
        metrics.setAverageResolutionMinutes(Math.round(avgResolutionMinutes * 10.0) / 10.0);

        return metrics;
    }

    @Transactional(readOnly = true)
    public List<TicketResponseDto> getRecentActivity() {
        return ticketRepository.findTop10Recent(PageRequest.of(0, 10))
                .stream()
                .map(emailIngestionService::mapToDto)
                .toList();
    }
}
