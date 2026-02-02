package com.workshop.ticketsystem.parser;

import com.workshop.ticketsystem.dto.CreateTicketRequest;
import com.workshop.ticketsystem.enums.DeviceType;
import com.workshop.ticketsystem.enums.TicketCategory;
import com.workshop.ticketsystem.enums.TicketPriority;
import com.workshop.ticketsystem.enums.TicketSource;
import com.workshop.ticketsystem.exception.FileParseException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class CsvFileParserTest {

    @Autowired
    private CsvFileParser csvFileParser;

    @Test
    void testParseValidCsv() throws Exception {
        String csvContent = "customer_id,customer_email,customer_name,subject,description,category,priority,assigned_to,tags,source,browser,device_type\n" +
                "C001,test@example.com,Test User,Test Subject,This is a test ticket description for testing.,BUG_REPORT,HIGH,admin,bug;urgent,EMAIL,Chrome,DESKTOP";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes()
        );

        List<CreateTicketRequest> tickets = csvFileParser.parse(file);

        assertThat(tickets).hasSize(1);
        CreateTicketRequest ticket = tickets.get(0);
        assertThat(ticket.getCustomerId()).isEqualTo("C001");
        assertThat(ticket.getCustomerEmail()).isEqualTo("test@example.com");
        assertThat(ticket.getCustomerName()).isEqualTo("Test User");
        assertThat(ticket.getSubject()).isEqualTo("Test Subject");
        assertThat(ticket.getDescription()).isEqualTo("This is a test ticket description for testing.");
        assertThat(ticket.getCategory()).isEqualTo(TicketCategory.BUG_REPORT);
        assertThat(ticket.getPriority()).isEqualTo(TicketPriority.HIGH);
        assertThat(ticket.getAssignedTo()).isEqualTo("admin");
        assertThat(ticket.getTags()).containsExactly("bug", "urgent");
        assertThat(ticket.getSource()).isEqualTo(TicketSource.EMAIL);
        assertThat(ticket.getBrowser()).isEqualTo("Chrome");
        assertThat(ticket.getDeviceType()).isEqualTo(DeviceType.DESKTOP);
    }

    @Test
    void testParseWithMissingOptionalFields() throws Exception {
        String csvContent = "customer_id,customer_email,customer_name,subject,description\n" +
                "C002,test2@example.com,Test User 2,Test Subject 2,This is another test ticket description.";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes()
        );

        List<CreateTicketRequest> tickets = csvFileParser.parse(file);

        assertThat(tickets).hasSize(1);
        CreateTicketRequest ticket = tickets.get(0);
        assertThat(ticket.getCustomerId()).isEqualTo("C002");
        assertThat(ticket.getCustomerEmail()).isEqualTo("test2@example.com");
        assertThat(ticket.getCustomerName()).isEqualTo("Test User 2");
        assertThat(ticket.getSubject()).isEqualTo("Test Subject 2");
        assertThat(ticket.getDescription()).isEqualTo("This is another test ticket description.");
        assertThat(ticket.getCategory()).isNull();
        assertThat(ticket.getPriority()).isNull();
        assertThat(ticket.getAssignedTo()).isNull();
        assertThat(ticket.getTags()).isNull();
    }

    @Test
    void testParseEmptyCsv() throws Exception {
        String csvContent = "customer_id,customer_email,customer_name,subject,description\n";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes()
        );

        List<CreateTicketRequest> tickets = csvFileParser.parse(file);

        assertThat(tickets).isEmpty();
    }

    @Test
    void testParseMalformedCsv() {
        String csvContent = "customer_id,customer_email,customer_name,subject,description\n" +
                "C001,test@example.com";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes()
        );

        assertThatThrownBy(() -> csvFileParser.parse(file))
                .isInstanceOf(FileParseException.class)
                .hasMessageContaining("Error parsing CSV record");
    }

    @Test
    void testParseWithInvalidEnumValues() {
        String csvContent = "customer_id,customer_email,customer_name,subject,description,category\n" +
                "C001,test@example.com,Test User,Test Subject,This is a test ticket description.,INVALID_CATEGORY";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes()
        );

        assertThatThrownBy(() -> csvFileParser.parse(file))
                .isInstanceOf(FileParseException.class)
                .hasMessageContaining("Error parsing CSV record");
    }

    @Test
    void testGetSupportedFormat() {
        assertThat(csvFileParser.getSupportedFormat()).isEqualTo("csv");
    }
}
