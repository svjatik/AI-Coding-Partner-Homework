package com.workshop.ticketsystem.service;

import com.workshop.ticketsystem.dto.ClassificationResult;
import com.workshop.ticketsystem.entity.Ticket;

import java.util.UUID;

public interface ClassificationService {

    ClassificationResult classify(Ticket ticket);

    ClassificationResult classifyById(UUID ticketId);
}
