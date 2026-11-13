package com.supportdesk.repository;

import com.supportdesk.model.Ticket;
import com.supportdesk.model.User;
import com.supportdesk.model.enums.Category;
import com.supportdesk.model.enums.HandlingType;
import com.supportdesk.model.enums.TicketStatus;
import com.supportdesk.model.enums.Urgency;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    List<Ticket> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);

    List<Ticket> findByAssignedAgent(User agent);

    long countByStatus(TicketStatus status);

    long countByHandlingType(HandlingType handlingType);

    long countByCategory(Category category);

    long countByUrgency(Urgency urgency);

    @Query("SELECT t FROM Ticket t ORDER BY t.createdAt DESC")
    List<Ticket> findTop10Recent(Pageable pageable);
}
