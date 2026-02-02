package com.workshop.ticketsystem.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.workshop.ticketsystem.dto.CreateTicketRequest;
import com.workshop.ticketsystem.exception.FileParseException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class XmlFileParser implements FileParser {

    private final XmlMapper xmlMapper = new XmlMapper();

    @Override
    public List<CreateTicketRequest> parse(MultipartFile file) throws IOException {
        try {
            String xmlContent = new String(file.getBytes());

            // Handle wrapper <tickets> element
            if (xmlContent.contains("<tickets>")) {
                TicketsWrapper wrapper = xmlMapper.readValue(xmlContent, TicketsWrapper.class);
                return wrapper.getTickets();
            }

            // Handle single ticket
            List<CreateTicketRequest> tickets = new ArrayList<>();
            tickets.add(xmlMapper.readValue(xmlContent, CreateTicketRequest.class));
            return tickets;
        } catch (IOException e) {
            throw new FileParseException("Error parsing XML file: " + e.getMessage(), e);
        }
    }

    @Override
    public String getSupportedFormat() {
        return "xml";
    }

    // Wrapper class for <tickets> element
    @JacksonXmlRootElement(localName = "tickets")
    private static class TicketsWrapper {
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "tickets")
        private List<CreateTicketRequest> tickets;

        public List<CreateTicketRequest> getTickets() {
            return tickets != null ? tickets : new ArrayList<>();
        }

        public void setTickets(List<CreateTicketRequest> tickets) {
            this.tickets = tickets;
        }
    }
}
