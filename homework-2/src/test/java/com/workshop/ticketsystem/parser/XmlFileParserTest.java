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
class XmlFileParserTest {

    @Autowired
    private XmlFileParser xmlFileParser;

    @Test
    void testParseMultipleTicketsWithWrapper() throws Exception {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<tickets>" +
                "<tickets>" +
                "<customerId>C001</customerId>" +
                "<customerEmail>test1@example.com</customerEmail>" +
                "<customerName>Test User 1</customerName>" +
                "<subject>Test Subject 1</subject>" +
                "<description>This is a test ticket description for XML testing.</description>" +
                "<category>BUG_REPORT</category>" +
                "<priority>HIGH</priority>" +
                "</tickets>" +
                "<tickets>" +
                "<customerId>C002</customerId>" +
                "<customerEmail>test2@example.com</customerEmail>" +
                "<customerName>Test User 2</customerName>" +
                "<subject>Test Subject 2</subject>" +
                "<description>This is another test ticket description for testing.</description>" +
                "</tickets>" +
                "</tickets>";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.xml",
                "application/xml",
                xmlContent.getBytes()
        );

        List<CreateTicketRequest> tickets = xmlFileParser.parse(file);

        assertThat(tickets).hasSize(2);
        assertThat(tickets.getFirst().getCustomerId()).isEqualTo("C001");
        assertThat(tickets.getFirst().getCategory()).isEqualTo(TicketCategory.BUG_REPORT);
        assertThat(tickets.getFirst().getPriority()).isEqualTo(TicketPriority.HIGH);
        assertThat(tickets.get(1).getCustomerId()).isEqualTo("C002");
    }

    @Test
    void testParseSingleTicket() throws Exception {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<CreateTicketRequest>" +
                "<customerId>C001</customerId>" +
                "<customerEmail>test@example.com</customerEmail>" +
                "<customerName>Test User</customerName>" +
                "<subject>Test Subject</subject>" +
                "<description>This is a test ticket description for single XML testing.</description>" +
                "</CreateTicketRequest>";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.xml",
                "application/xml",
                xmlContent.getBytes()
        );

        List<CreateTicketRequest> tickets = xmlFileParser.parse(file);

        assertThat(tickets).hasSize(1);
        CreateTicketRequest ticket = tickets.getFirst();
        assertThat(ticket.getCustomerId()).isEqualTo("C001");
        assertThat(ticket.getCustomerEmail()).isEqualTo("test@example.com");
        assertThat(ticket.getCustomerName()).isEqualTo("Test User");
        assertThat(ticket.getSubject()).isEqualTo("Test Subject");
        assertThat(ticket.getDescription()).isEqualTo("This is a test ticket description for single XML testing.");
    }

    @Test
    void testParseMalformedXml() {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<tickets>" +
                "<customerId>C001</customerId>" +
                "<customerEmail>test@example.com</customerEmail>";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.xml",
                "application/xml",
                xmlContent.getBytes()
        );

        assertThatThrownBy(() -> xmlFileParser.parse(file))
                .isInstanceOf(FileParseException.class)
                .hasMessageContaining("Error parsing XML file");
    }

    @Test
    void testParseEmptyXml() throws Exception {
        String xmlContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<tickets></tickets>";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.xml",
                "application/xml",
                xmlContent.getBytes()
        );

        List<CreateTicketRequest> tickets = xmlFileParser.parse(file);

        assertThat(tickets).isEmpty();
    }

    @Test
    void testGetSupportedFormat() {
        assertThat(xmlFileParser.getSupportedFormat()).isEqualTo("xml");
    }
}
