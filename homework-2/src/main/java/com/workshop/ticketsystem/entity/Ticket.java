package com.workshop.ticketsystem.entity;

import com.workshop.ticketsystem.enums.TicketCategory;
import com.workshop.ticketsystem.enums.TicketPriority;
import com.workshop.ticketsystem.enums.TicketStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "Customer ID is required")
    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @NotBlank(message = "Customer email is required")
    @Email(message = "Invalid email format")
    @Column(name = "customer_email", nullable = false)
    private String customerEmail;

    @NotBlank(message = "Customer name is required")
    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @NotBlank(message = "Subject is required")
    @Size(min = 1, max = 200, message = "Subject must be between 1 and 200 characters")
    @Column(nullable = false, length = 200)
    private String subject;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    @Column(nullable = false, length = 2000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TicketStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "assigned_to")
    private String assignedTo;

    @ElementCollection
    @CollectionTable(name = "ticket_tags", joinColumns = @JoinColumn(name = "ticket_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Embedded
    private TicketMetadata metadata;

    @PrePersist
    public void prePersist() {
        if (status == null) {
            status = TicketStatus.NEW;
        }
        if (category == null) {
            category = TicketCategory.OTHER;
        }
        if (priority == null) {
            priority = TicketPriority.MEDIUM;
        }
    }

    @PreUpdate
    public void preUpdate() {
        if (status == TicketStatus.RESOLVED && resolvedAt == null) {
            resolvedAt = LocalDateTime.now();
        }
    }
}
