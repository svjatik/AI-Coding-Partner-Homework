package com.workshop.ticketsystem.dto;

import com.workshop.ticketsystem.enums.TicketCategory;
import com.workshop.ticketsystem.enums.TicketPriority;
import com.workshop.ticketsystem.enums.TicketStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTicketRequest {

    @Size(min = 1, max = 200, message = "Subject must be between 1 and 200 characters")
    private String subject;

    @Size(min = 10, max = 2000, message = "Description must be between 10 and 2000 characters")
    private String description;

    private TicketCategory category;

    private TicketPriority priority;

    private TicketStatus status;

    private String assignedTo;

    private List<String> tags;
}
