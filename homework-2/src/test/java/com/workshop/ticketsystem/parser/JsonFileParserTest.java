package com.workshop.ticketsystem.parser;

import com.workshop.ticketsystem.dto.CreateTicketRequest;
import com.workshop.ticketsystem.enums.TicketCategory;
import com.workshop.ticketsystem.enums.TicketPriority;
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
class JsonFileParserTest {

    @Autowired
    private JsonFileParser jsonFileParser;

    @Test
    void testParseJsonArray() throws Exception {
        String jsonContent = "[" +
                "{\"customerId\":\"C001\",\"customerEmail\":\"test1@example.com\"," +
                "\"customerName\":\"Test User 1\",\"subject\":\"Test Subject 1\"," +
                "\"description\":\"This is a test ticket description for JSON import testing.\"," +
                "\"category\":\"BUG_REPORT\",\"priority\":\"HIGH\"}," +
                "{\"customerId\":\"C002\",\"customerEmail\":\"test2@example.com\"," +
                "\"customerName\":\"Test User 2\",\"subject\":\"Test Subject 2\"," +
                "\"description\":\"This is another test ticket description for testing.\"}" +
                "]";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                jsonContent.getBytes()
        );

        List<CreateTicketRequest> tickets = jsonFileParser.parse(file);

        assertThat(tickets).hasSize(2);
        assertThat(tickets.getFirst().getCustomerId()).isEqualTo("C001");
        assertThat(tickets.getFirst().getCategory()).isEqualTo(TicketCategory.BUG_REPORT);
        assertThat(tickets.getFirst().getPriority()).isEqualTo(TicketPriority.HIGH);
        assertThat(tickets.get(1).getCustomerId()).isEqualTo("C002");
    }

    @Test
    void testParseSingleJsonObject() throws Exception {
        String jsonContent = "{\"customerId\":\"C001\",\"customerEmail\":\"test@example.com\"," +
                "\"customerName\":\"Test User\",\"subject\":\"Test Subject\"," +
                "\"description\":\"This is a test ticket description for single object testing.\"}";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                jsonContent.getBytes()
        );

        List<CreateTicketRequest> tickets = jsonFileParser.parse(file);

        assertThat(tickets).hasSize(1);
        CreateTicketRequest ticket = tickets.getFirst();
        assertThat(ticket.getCustomerId()).isEqualTo("C001");
        assertThat(ticket.getCustomerEmail()).isEqualTo("test@example.com");
        assertThat(ticket.getCustomerName()).isEqualTo("Test User");
        assertThat(ticket.getSubject()).isEqualTo("Test Subject");
        assertThat(ticket.getDescription()).isEqualTo("This is a test ticket description for single object testing.");
    }

    @Test
    void testParseNestedTicketsObject() throws Exception {
        String jsonContent = "{\"tickets\":[" +
                "{\"customerId\":\"C001\",\"customerEmail\":\"test1@example.com\"," +
                "\"customerName\":\"Test User 1\",\"subject\":\"Test Subject 1\"," +
                "\"description\":\"This is a test ticket description for nested object testing.\"}," +
                "{\"customerId\":\"C002\",\"customerEmail\":\"test2@example.com\"," +
                "\"customerName\":\"Test User 2\",\"subject\":\"Test Subject 2\"," +
                "\"description\":\"This is another test ticket description for testing.\"}" +
                "]}";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                jsonContent.getBytes()
        );

        List<CreateTicketRequest> tickets = jsonFileParser.parse(file);

        assertThat(tickets).hasSize(2);
        assertThat(tickets.getFirst().getCustomerId()).isEqualTo("C001");
        assertThat(tickets.get(1).getCustomerId()).isEqualTo("C002");
    }

    @Test
    void testParseEmptyJson() {
        String jsonContent = "\"invalid\"";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                jsonContent.getBytes()
        );

        assertThatThrownBy(() -> jsonFileParser.parse(file))
                .isInstanceOf(FileParseException.class)
                .hasMessageContaining("Invalid JSON structure");
    }

    @Test
    void testGetSupportedFormat() {
        assertThat(jsonFileParser.getSupportedFormat()).isEqualTo("json");
    }
}
