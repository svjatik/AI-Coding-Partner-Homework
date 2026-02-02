package com.workshop.ticketsystem.service;

import com.workshop.ticketsystem.dto.ClassificationResult;
import com.workshop.ticketsystem.dto.CreateTicketRequest;
import com.workshop.ticketsystem.dto.TicketDto;
import com.workshop.ticketsystem.dto.UpdateTicketRequest;
import com.workshop.ticketsystem.entity.Ticket;
import com.workshop.ticketsystem.entity.TicketMetadata;
import com.workshop.ticketsystem.enums.TicketCategory;
import com.workshop.ticketsystem.enums.TicketPriority;
import com.workshop.ticketsystem.enums.TicketStatus;
import com.workshop.ticketsystem.exception.TicketNotFoundException;
import com.workshop.ticketsystem.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketServiceImpl implements TicketService {

    private final TicketRepository ticketRepository;
    private final ClassificationService classificationService;

    @Override
    @Transactional
    public TicketDto createTicket(CreateTicketRequest request) {
        Ticket ticket = mapToEntity(request);

        // Auto-classify if requested
        if (Boolean.TRUE.equals(request.getAutoClassify())) {
            ClassificationResult result = classificationService.classify(ticket);
            if (ticket.getCategory() == null || ticket.getCategory() == TicketCategory.OTHER) {
                ticket.setCategory(result.getCategory());
            }
            if (ticket.getPriority() == null || ticket.getPriority() == TicketPriority.MEDIUM) {
                ticket.setPriority(result.getPriority());
            }
        }

        Ticket savedTicket = ticketRepository.save(ticket);
        return mapToDto(savedTicket);
    }

    @Override
    @Transactional(readOnly = true)
    public TicketDto getTicketById(UUID id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
        return mapToDto(ticket);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDto> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TicketDto> getTicketsByFilters(TicketCategory category, TicketPriority priority, TicketStatus status) {
        return ticketRepository.findByFilters(category, priority, status).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TicketDto updateTicket(UUID id, UpdateTicketRequest request) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));

        if (request.getSubject() != null) {
            ticket.setSubject(request.getSubject());
        }
        if (request.getDescription() != null) {
            ticket.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            ticket.setCategory(request.getCategory());
        }
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }
        if (request.getStatus() != null) {
            ticket.setStatus(request.getStatus());
        }
        if (request.getAssignedTo() != null) {
            ticket.setAssignedTo(request.getAssignedTo());
        }
        if (request.getTags() != null) {
            ticket.setTags(request.getTags());
        }

        Ticket updatedTicket = ticketRepository.save(ticket);
        return mapToDto(updatedTicket);
    }

    @Override
    @Transactional
    public void deleteTicket(UUID id) {
        if (!ticketRepository.existsById(id)) {
            throw new TicketNotFoundException(id);
        }
        ticketRepository.deleteById(id);
    }

    private Ticket mapToEntity(CreateTicketRequest request) {
        Ticket ticket = new Ticket();
        ticket.setCustomerId(request.getCustomerId());
        ticket.setCustomerEmail(request.getCustomerEmail());
        ticket.setCustomerName(request.getCustomerName());
        ticket.setSubject(request.getSubject());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(request.getCategory());
        ticket.setPriority(request.getPriority());
        ticket.setAssignedTo(request.getAssignedTo());
        ticket.setTags(request.getTags());

        // Set metadata if provided
        if (request.getSource() != null || request.getBrowser() != null || request.getDeviceType() != null) {
            TicketMetadata metadata = new TicketMetadata();
            metadata.setSource(request.getSource());
            metadata.setBrowser(request.getBrowser());
            metadata.setDeviceType(request.getDeviceType());
            ticket.setMetadata(metadata);
        }

        return ticket;
    }

    private TicketDto mapToDto(Ticket ticket) {
        TicketDto dto = new TicketDto();
        dto.setId(ticket.getId());
        dto.setCustomerId(ticket.getCustomerId());
        dto.setCustomerEmail(ticket.getCustomerEmail());
        dto.setCustomerName(ticket.getCustomerName());
        dto.setSubject(ticket.getSubject());
        dto.setDescription(ticket.getDescription());
        dto.setCategory(ticket.getCategory());
        dto.setPriority(ticket.getPriority());
        dto.setStatus(ticket.getStatus());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        dto.setResolvedAt(ticket.getResolvedAt());
        dto.setAssignedTo(ticket.getAssignedTo());
        dto.setTags(ticket.getTags());
        dto.setMetadata(ticket.getMetadata());
        return dto;
    }
}
