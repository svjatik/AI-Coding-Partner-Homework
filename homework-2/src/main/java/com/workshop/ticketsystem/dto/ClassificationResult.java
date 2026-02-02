package com.workshop.ticketsystem.dto;

import com.workshop.ticketsystem.enums.TicketCategory;
import com.workshop.ticketsystem.enums.TicketPriority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassificationResult {

    private TicketCategory category;
    private TicketPriority priority;
    private Double confidenceScore;
    private String reasoning;
    private List<String> keywordsFound;
}
