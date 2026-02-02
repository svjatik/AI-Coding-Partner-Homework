package com.workshop.ticketsystem.service;

import com.workshop.ticketsystem.dto.CreateTicketRequest;
import com.workshop.ticketsystem.dto.ImportSummaryResponse;
import com.workshop.ticketsystem.exception.FileParseException;
import com.workshop.ticketsystem.parser.FileParser;
import com.workshop.ticketsystem.parser.ParserFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImportServiceImpl implements ImportService {

    private final ParserFactory parserFactory;
    private final TicketService ticketService;
    private final Validator validator;

    @Override
    @Transactional
    public ImportSummaryResponse importTickets(MultipartFile file, String format, boolean autoClassify) {
        ImportSummaryResponse summary = new ImportSummaryResponse();

        try {
            // Get appropriate parser
            FileParser parser = parserFactory.getParser(format);

            // Parse file
            List<CreateTicketRequest> tickets = parser.parse(file);
            summary.setTotalRecords(tickets.size());

            // Process each ticket
            int successCount = 0;
            int failCount = 0;

            for (int i = 0; i < tickets.size(); i++) {
                CreateTicketRequest ticket = tickets.get(i);
                try {
                    // Validate ticket
                    Set<ConstraintViolation<CreateTicketRequest>> violations = validator.validate(ticket);
                    if (!violations.isEmpty()) {
                        String errors = violations.stream()
                                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                                .reduce((a, b) -> a + ", " + b)
                                .orElse("Unknown validation error");
                        summary.addError("Record " + (i + 1) + ": " + errors);
                        failCount++;
                        continue;
                    }

                    // Set auto-classify flag
                    ticket.setAutoClassify(autoClassify);

                    // Create ticket
                    ticketService.createTicket(ticket);
                    successCount++;

                } catch (Exception e) {
                    log.error("Error importing ticket at index {}: {}", i, e.getMessage(), e);
                    summary.addError("Record " + (i + 1) + ": " + e.getMessage());
                    failCount++;
                }
            }

            summary.setSuccessfulImports(successCount);
            summary.setFailedImports(failCount);

        } catch (FileParseException e) {
            log.error("Error parsing file: {}", e.getMessage(), e);
            summary.addError("File parsing error: " + e.getMessage());
            summary.setFailedImports(summary.getTotalRecords());
        } catch (Exception e) {
            log.error("Unexpected error during import: {}", e.getMessage(), e);
            summary.addError("Unexpected error: " + e.getMessage());
            summary.setFailedImports(summary.getTotalRecords());
        }

        return summary;
    }
}
