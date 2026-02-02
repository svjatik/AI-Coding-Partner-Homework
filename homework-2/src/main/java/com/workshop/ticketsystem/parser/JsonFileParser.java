package com.workshop.ticketsystem.parser;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workshop.ticketsystem.dto.CreateTicketRequest;
import com.workshop.ticketsystem.exception.FileParseException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class JsonFileParser implements FileParser {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<CreateTicketRequest> parse(MultipartFile file) throws IOException {
        try {
            JsonNode rootNode = objectMapper.readTree(file.getInputStream());

            // Handle array format
            if (rootNode.isArray()) {
                return objectMapper.convertValue(rootNode, new TypeReference<List<CreateTicketRequest>>() {});
            }

            // Handle object with "tickets" field
            if (rootNode.has("tickets")) {
                JsonNode ticketsNode = rootNode.get("tickets");
                if (ticketsNode.isArray()) {
                    return objectMapper.convertValue(ticketsNode, new TypeReference<List<CreateTicketRequest>>() {});
                }
            }

            // Handle single object
            if (rootNode.isObject()) {
                List<CreateTicketRequest> tickets = new ArrayList<>();
                tickets.add(objectMapper.convertValue(rootNode, CreateTicketRequest.class));
                return tickets;
            }

            throw new FileParseException("Invalid JSON structure. Expected array or object with 'tickets' field.");
        } catch (IOException e) {
            throw new FileParseException("Error parsing JSON file: " + e.getMessage(), e);
        }
    }

    @Override
    public String getSupportedFormat() {
        return "json";
    }
}
