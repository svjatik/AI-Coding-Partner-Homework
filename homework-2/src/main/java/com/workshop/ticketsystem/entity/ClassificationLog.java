package com.workshop.ticketsystem.entity;

import com.workshop.ticketsystem.enums.TicketCategory;
import com.workshop.ticketsystem.enums.TicketPriority;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "classification_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ticket_id", nullable = false)
    private UUID ticketId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketCategory suggestedCategory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPriority suggestedPriority;

    @Column(name = "confidence_score", nullable = false)
    private Double confidenceScore;

    @Column(length = 1000)
    private String reasoning;

    @ElementCollection
    @CollectionTable(name = "classification_keywords", joinColumns = @JoinColumn(name = "log_id"))
    @Column(name = "keyword")
    private List<String> keywordsFound = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "classified_at", nullable = false, updatable = false)
    private LocalDateTime classifiedAt;
}
