package com.workshop.ticketsystem.service;

import com.workshop.ticketsystem.dto.ClassificationResult;
import com.workshop.ticketsystem.entity.Ticket;
import com.workshop.ticketsystem.enums.TicketCategory;
import com.workshop.ticketsystem.enums.TicketPriority;
import com.workshop.ticketsystem.repository.ClassificationLogRepository;
import com.workshop.ticketsystem.repository.TicketRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ClassificationServiceTest {

    @Autowired
    private ClassificationService classificationService;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private ClassificationLogRepository classificationLogRepository;

    @Test
    void testClassifyAccountAccess() {
        Ticket ticket = createTicket("Cannot login", "I forgot my password and cannot reset it");
        ClassificationResult result = classificationService.classify(ticket);

        assertThat(result.getCategory()).isEqualTo(TicketCategory.ACCOUNT_ACCESS);
        assertThat(result.getConfidenceScore()).isGreaterThan(0.0);
    }

    @Test
    void testClassifyTechnicalIssue() {
        Ticket ticket = createTicket("Application error", "Getting error message and application crashes");
        ClassificationResult result = classificationService.classify(ticket);

        assertThat(result.getCategory()).isEqualTo(TicketCategory.TECHNICAL_ISSUE);
        assertThat(result.getConfidenceScore()).isGreaterThan(0.0);
    }

    @Test
    void testClassifyBillingQuestion() {
        Ticket ticket = createTicket("Invoice issue", "I was charged twice. Need a refund for my payment");
        ClassificationResult result = classificationService.classify(ticket);

        assertThat(result.getCategory()).isEqualTo(TicketCategory.BILLING_QUESTION);
        assertThat(result.getConfidenceScore()).isGreaterThan(0.0);
    }

    @Test
    void testClassifyFeatureRequest() {
        Ticket ticket = createTicket("New feature", "Would like to add dark mode feature to the application");
        ClassificationResult result = classificationService.classify(ticket);

        assertThat(result.getCategory()).isEqualTo(TicketCategory.FEATURE_REQUEST);
        assertThat(result.getConfidenceScore()).isGreaterThan(0.0);
    }

    @Test
    void testClassifyBugReport() {
        Ticket ticket = createTicket("Bug found", "There is a bug in the system showing incorrect data");
        ClassificationResult result = classificationService.classify(ticket);

        assertThat(result.getCategory()).isEqualTo(TicketCategory.BUG_REPORT);
        assertThat(result.getConfidenceScore()).isGreaterThan(0.0);
    }

    @Test
    void testClassifyOther() {
        Ticket ticket = createTicket("General question", "I have a general question about your services");
        ClassificationResult result = classificationService.classify(ticket);

        assertThat(result.getCategory()).isIn(TicketCategory.OTHER, TicketCategory.FEATURE_REQUEST);
    }

    @Test
    void testClassifyUrgentPriority() {
        Ticket ticket = createTicket("Critical issue", "This is urgent and critical. Production down and cannot access");
        ClassificationResult result = classificationService.classify(ticket);

        assertThat(result.getPriority()).isEqualTo(TicketPriority.URGENT);
        assertThat(result.getConfidenceScore()).isGreaterThan(0.0);
    }

    @Test
    void testClassifyHighPriority() {
        Ticket ticket = createTicket("Important issue", "This is important and high priority issue that needs attention soon");
        ClassificationResult result = classificationService.classify(ticket);

        assertThat(result.getPriority()).isEqualTo(TicketPriority.HIGH);
        assertThat(result.getConfidenceScore()).isGreaterThan(0.0);
    }

    @Test
    void testClassifyMediumPriority() {
        Ticket ticket = createTicket("Normal issue", "This is a moderate normal issue when possible");
        ClassificationResult result = classificationService.classify(ticket);

        assertThat(result.getPriority()).isEqualTo(TicketPriority.MEDIUM);
    }

    @Test
    void testClassifyLowPriority() {
        Ticket ticket = createTicket("Minor issue", "This is a low priority minor cosmetic issue not urgent");
        ClassificationResult result = classificationService.classify(ticket);

        assertThat(result.getPriority()).isEqualTo(TicketPriority.LOW);
        assertThat(result.getConfidenceScore()).isGreaterThan(0.0);
    }

    private Ticket createTicket(String subject, String description) {
        Ticket ticket = new Ticket();
        ticket.setCustomerId("TEST001");
        ticket.setCustomerEmail("test@example.com");
        ticket.setCustomerName("Test User");
        ticket.setSubject(subject);
        ticket.setDescription(description);
        return ticketRepository.save(ticket);
    }
}
