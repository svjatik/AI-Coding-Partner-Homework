package com.workshop.ticketsystem.service;

import com.workshop.ticketsystem.dto.CreateTicketRequest;
import com.workshop.ticketsystem.dto.TicketDto;
import com.workshop.ticketsystem.dto.UpdateTicketRequest;
import com.workshop.ticketsystem.enums.TicketCategory;
import com.workshop.ticketsystem.enums.TicketPriority;
import com.workshop.ticketsystem.enums.TicketStatus;

import java.util.List;
import java.util.UUID;

public interface TicketService {

    TicketDto createTicket(CreateTicketRequest request);

    TicketDto getTicketById(UUID id);

    List<TicketDto> getAllTickets();

    List<TicketDto> getTicketsByFilters(TicketCategory category, TicketPriority priority, TicketStatus status);

    TicketDto updateTicket(UUID id, UpdateTicketRequest request);

    void deleteTicket(UUID id);
}
