package com.workshop.ticketsystem.entity;

import com.workshop.ticketsystem.enums.TicketCategory;
import com.workshop.ticketsystem.enums.TicketPriority;
import com.workshop.ticketsystem.enums.TicketStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class TicketModelTest {

    @Autowired
    private Validator validator;

    @Test
    void testValidTicketCreation() {
        Ticket ticket = new Ticket();
        ticket.setCustomerId("C001");
        ticket.setCustomerEmail("test@example.com");
        ticket.setCustomerName("Test User");
        ticket.setSubject("Test Subject");
        ticket.setDescription("This is a valid test ticket description for testing purposes.");
        ticket.setCategory(TicketCategory.BUG_REPORT);
        ticket.setPriority(TicketPriority.HIGH);
        ticket.setStatus(TicketStatus.NEW);

        Set<ConstraintViolation<Ticket>> violations = validator.validate(ticket);

        assertThat(violations).isEmpty();
    }

    @Test
    void testSubjectTooShort() {
        Ticket ticket = new Ticket();
        ticket.setCustomerId("C001");
        ticket.setCustomerEmail("test@example.com");
        ticket.setCustomerName("Test User");
        ticket.setSubject("");
        ticket.setDescription("This is a valid test ticket description for testing purposes.");

        Set<ConstraintViolation<Ticket>> violations = validator.validate(ticket);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("subject"));
    }

    @Test
    void testSubjectTooLong() {
        Ticket ticket = new Ticket();
        ticket.setCustomerId("C001");
        ticket.setCustomerEmail("test@example.com");
        ticket.setCustomerName("Test User");
        ticket.setSubject("a".repeat(201));
        ticket.setDescription("This is a valid test ticket description for testing purposes.");

        Set<ConstraintViolation<Ticket>> violations = validator.validate(ticket);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("subject") &&
            v.getMessage().contains("between 1 and 200"));
    }

    @Test
    void testDescriptionTooShort() {
        Ticket ticket = new Ticket();
        ticket.setCustomerId("C001");
        ticket.setCustomerEmail("test@example.com");
        ticket.setCustomerName("Test User");
        ticket.setSubject("Test Subject");
        ticket.setDescription("Short");

        Set<ConstraintViolation<Ticket>> violations = validator.validate(ticket);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("description") &&
            v.getMessage().contains("between 10 and 2000"));
    }

    @Test
    void testDescriptionTooLong() {
        Ticket ticket = new Ticket();
        ticket.setCustomerId("C001");
        ticket.setCustomerEmail("test@example.com");
        ticket.setCustomerName("Test User");
        ticket.setSubject("Test Subject");
        ticket.setDescription("a".repeat(2001));

        Set<ConstraintViolation<Ticket>> violations = validator.validate(ticket);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("description") &&
            v.getMessage().contains("between 10 and 2000"));
    }

    @Test
    void testInvalidEmailFormat() {
        Ticket ticket = new Ticket();
        ticket.setCustomerId("C001");
        ticket.setCustomerEmail("invalid-email");
        ticket.setCustomerName("Test User");
        ticket.setSubject("Test Subject");
        ticket.setDescription("This is a valid test ticket description for testing purposes.");

        Set<ConstraintViolation<Ticket>> violations = validator.validate(ticket);

        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
            v.getPropertyPath().toString().equals("customerEmail") &&
            v.getMessage().contains("Invalid email format"));
    }

    @Test
    void testDefaultStatusIsNew() {
        Ticket ticket = new Ticket();
        ticket.setCustomerId("C001");
        ticket.setCustomerEmail("test@example.com");
        ticket.setCustomerName("Test User");
        ticket.setSubject("Test Subject");
        ticket.setDescription("This is a valid test ticket description for testing purposes.");

        ticket.prePersist();

        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.NEW);
    }

    @Test
    void testDefaultCategoryIsOther() {
        Ticket ticket = new Ticket();
        ticket.setCustomerId("C001");
        ticket.setCustomerEmail("test@example.com");
        ticket.setCustomerName("Test User");
        ticket.setSubject("Test Subject");
        ticket.setDescription("This is a valid test ticket description for testing purposes.");

        ticket.prePersist();

        assertThat(ticket.getCategory()).isEqualTo(TicketCategory.OTHER);
    }

    @Test
    void testResolvedTimestampSetWhenStatusChangedToResolved() {
        Ticket ticket = new Ticket();
        ticket.setCustomerId("C001");
        ticket.setCustomerEmail("test@example.com");
        ticket.setCustomerName("Test User");
        ticket.setSubject("Test Subject");
        ticket.setDescription("This is a valid test ticket description for testing purposes.");
        ticket.setStatus(TicketStatus.NEW);

        assertThat(ticket.getResolvedAt()).isNull();

        ticket.setStatus(TicketStatus.RESOLVED);
        ticket.preUpdate();

        assertThat(ticket.getResolvedAt()).isNotNull();
    }
}
