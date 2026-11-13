package com.supportdesk.repository;

import com.supportdesk.model.Ticket;
import com.supportdesk.model.enums.Category;
import com.supportdesk.model.enums.HandlingType;
import com.supportdesk.model.enums.TicketStatus;
import com.supportdesk.model.enums.Urgency;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TicketSpecification {

    public static Specification<Ticket> withFilters(
            TicketStatus status,
            Urgency urgency,
            Category category,
            HandlingType handlingType,
            String search
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (urgency != null) {
                predicates.add(cb.equal(root.get("urgency"), urgency));
            }
            if (category != null) {
                predicates.add(cb.equal(root.get("category"), category));
            }
            if (handlingType != null) {
                predicates.add(cb.equal(root.get("handlingType"), handlingType));
            }
            if (search != null && !search.trim().isEmpty()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                Predicate subjectMatch = cb.like(cb.lower(root.get("subject")), pattern);
                Predicate emailMatch = cb.like(cb.lower(root.get("customerEmail")), pattern);
                Predicate ticketNumMatch = cb.like(cb.lower(root.get("ticketNumber")), pattern);
                predicates.add(cb.or(subjectMatch, emailMatch, ticketNumMatch));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
