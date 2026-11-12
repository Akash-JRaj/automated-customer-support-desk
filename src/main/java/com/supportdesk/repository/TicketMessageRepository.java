package com.supportdesk.repository;

import com.supportdesk.model.Ticket;
import com.supportdesk.model.TicketMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketMessageRepository extends JpaRepository<TicketMessage, Long> {
    List<TicketMessage> findByTicketOrderBySentAtAsc(Ticket ticket);
}
