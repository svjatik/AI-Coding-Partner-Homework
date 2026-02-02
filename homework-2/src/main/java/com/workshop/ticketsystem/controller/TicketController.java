package com.workshop.ticketsystem.controller;

import com.workshop.ticketsystem.dto.*;
import com.workshop.ticketsystem.enums.TicketCategory;
import com.workshop.ticketsystem.enums.TicketPriority;
import com.workshop.ticketsystem.enums.TicketStatus;
import com.workshop.ticketsystem.service.ClassificationService;
import com.workshop.ticketsystem.service.ImportService;
import com.workshop.ticketsystem.service.TicketService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/tickets")
@RequiredArgsConstructor
@Tag(name = "Ticket Management", description = "APIs for managing customer support tickets")
public class TicketController {

    private final TicketService ticketService;
    private final ImportService importService;
    private final ClassificationService classificationService;

    @PostMapping
    @Operation(summary = "Create a new ticket", description = "Creates a new customer support ticket with optional auto-classification")
    public ResponseEntity<TicketDto> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        TicketDto ticket = ticketService.createTicket(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    @PostMapping("/import")
    @Operation(summary = "Import tickets from file", description = "Bulk import tickets from CSV, JSON, or XML file")
    public ResponseEntity<ImportSummaryResponse> importTickets(
            @RequestParam("file") MultipartFile file,
            @RequestParam("format") String format,
            @RequestParam(value = "autoClassify", defaultValue = "false") boolean autoClassify) {
        ImportSummaryResponse summary = importService.importTickets(file, format, autoClassify);
        return ResponseEntity.ok(summary);
    }

    @GetMapping
    @Operation(summary = "Get all tickets", description = "Retrieves all tickets with optional filtering by category, priority, and status")
    public ResponseEntity<List<TicketDto>> getAllTickets(
            @RequestParam(required = false) TicketCategory category,
            @RequestParam(required = false) TicketPriority priority,
            @RequestParam(required = false) TicketStatus status) {

        List<TicketDto> tickets;
        if (category != null || priority != null || status != null) {
            tickets = ticketService.getTicketsByFilters(category, priority, status);
        } else {
            tickets = ticketService.getAllTickets();
        }
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get ticket by ID", description = "Retrieves a specific ticket by its UUID")
    public ResponseEntity<TicketDto> getTicketById(@PathVariable UUID id) {
        TicketDto ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update ticket", description = "Updates an existing ticket")
    public ResponseEntity<TicketDto> updateTicket(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTicketRequest request) {
        TicketDto ticket = ticketService.updateTicket(id, request);
        return ResponseEntity.ok(ticket);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete ticket", description = "Deletes a ticket by its UUID")
    public ResponseEntity<Void> deleteTicket(@PathVariable UUID id) {
        ticketService.deleteTicket(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/auto-classify")
    @Operation(summary = "Auto-classify ticket", description = "Automatically classifies ticket category and priority using keyword-based analysis")
    public ResponseEntity<ClassificationResult> autoClassify(@PathVariable UUID id) {
        ClassificationResult result = classificationService.classifyById(id);
        return ResponseEntity.ok(result);
    }
}
