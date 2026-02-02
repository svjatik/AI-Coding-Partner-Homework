package com.workshop.ticketsystem.repository;

import com.workshop.ticketsystem.entity.Ticket;
import com.workshop.ticketsystem.enums.TicketCategory;
import com.workshop.ticketsystem.enums.TicketPriority;
import com.workshop.ticketsystem.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    List<Ticket> findByCategory(TicketCategory category);

    List<Ticket> findByPriority(TicketPriority priority);

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByCategoryAndPriority(TicketCategory category, TicketPriority priority);

    List<Ticket> findByCategoryAndStatus(TicketCategory category, TicketStatus status);

    List<Ticket> findByPriorityAndStatus(TicketPriority priority, TicketStatus status);

    List<Ticket> findByCategoryAndPriorityAndStatus(
            TicketCategory category,
            TicketPriority priority,
            TicketStatus status
    );

    @Query("SELECT t FROM Ticket t WHERE " +
           "(:category IS NULL OR t.category = :category) AND " +
           "(:priority IS NULL OR t.priority = :priority) AND " +
           "(:status IS NULL OR t.status = :status)")
    List<Ticket> findByFilters(
            @Param("category") TicketCategory category,
            @Param("priority") TicketPriority priority,
            @Param("status") TicketStatus status
    );

    List<Ticket> findByCustomerId(String customerId);

    List<Ticket> findByAssignedTo(String assignedTo);
}
