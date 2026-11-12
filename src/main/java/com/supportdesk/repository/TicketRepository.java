package com.supportdesk.repository;

import com.supportdesk.model.Ticket;
import com.supportdesk.model.User;
import com.supportdesk.model.enums.Category;
import com.supportdesk.model.enums.HandlingType;
import com.supportdesk.model.enums.TicketStatus;
import com.supportdesk.model.enums.Urgency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {

    Optional<Ticket> findByTicketNumber(String ticketNumber);

    List<Ticket> findByCustomerEmailOrderByCreatedAtDesc(String customerEmail);

    List<Ticket> findByAssignedAgent(User agent);

    long countByStatus(TicketStatus status);

    long countByHandlingType(HandlingType handlingType);

    long countByCategory(Category category);

    long countByUrgency(Urgency urgency);

    @Query("SELECT t FROM Ticket t WHERE " +
           "(:status IS NULL OR t.status = :status) AND " +
           "(:urgency IS NULL OR t.urgency = :urgency) AND " +
           "(:category IS NULL OR t.category = :category) AND " +
           "(:handlingType IS NULL OR t.handlingType = :handlingType) AND " +
           "(:search IS NULL OR LOWER(t.subject) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "  OR LOWER(t.customerEmail) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "  OR LOWER(t.ticketNumber) LIKE LOWER(CONCAT('%', :search, '%'))) " +
           "ORDER BY t.createdAt DESC")
    Page<Ticket> findWithFilters(
            @Param("status") TicketStatus status,
            @Param("urgency") Urgency urgency,
            @Param("category") Category category,
            @Param("handlingType") HandlingType handlingType,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT t FROM Ticket t ORDER BY t.createdAt DESC")
    List<Ticket> findTop10Recent(Pageable pageable);
}
