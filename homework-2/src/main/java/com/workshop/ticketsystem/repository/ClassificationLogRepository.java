package com.workshop.ticketsystem.repository;

import com.workshop.ticketsystem.entity.ClassificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ClassificationLogRepository extends JpaRepository<ClassificationLog, UUID> {

    List<ClassificationLog> findByTicketId(UUID ticketId);

    List<ClassificationLog> findByTicketIdOrderByClassifiedAtDesc(UUID ticketId);
}
