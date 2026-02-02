package com.workshop.ticketsystem.parser;

import com.workshop.ticketsystem.dto.CreateTicketRequest;
import com.workshop.ticketsystem.enums.DeviceType;
import com.workshop.ticketsystem.enums.TicketCategory;
import com.workshop.ticketsystem.enums.TicketPriority;
import com.workshop.ticketsystem.enums.TicketSource;
import com.workshop.ticketsystem.exception.FileParseException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CsvFileParser implements FileParser {

    @Override
    public List<CreateTicketRequest> parse(MultipartFile file) throws IOException {
        List<CreateTicketRequest> tickets = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream()));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreEmptyLines(true)
                     .setTrim(true)
                     .build())) {

            for (CSVRecord record : csvParser) {
                try {
                    CreateTicketRequest ticket = new CreateTicketRequest();
                    ticket.setCustomerId(record.get("customer_id"));
                    ticket.setCustomerEmail(record.get("customer_email"));
                    ticket.setCustomerName(record.get("customer_name"));
                    ticket.setSubject(record.get("subject"));
                    ticket.setDescription(record.get("description"));

                    // Optional fields
                    if (record.isMapped("category") && !record.get("category").isEmpty()) {
                        ticket.setCategory(TicketCategory.valueOf(record.get("category").toUpperCase()));
                    }

                    if (record.isMapped("priority") && !record.get("priority").isEmpty()) {
                        ticket.setPriority(TicketPriority.valueOf(record.get("priority").toUpperCase()));
                    }

                    if (record.isMapped("assigned_to") && !record.get("assigned_to").isEmpty()) {
                        ticket.setAssignedTo(record.get("assigned_to"));
                    }

                    if (record.isMapped("tags") && !record.get("tags").isEmpty()) {
                        String tagsStr = record.get("tags");
                        ticket.setTags(Arrays.asList(tagsStr.split(";")));
                    }

                    if (record.isMapped("source") && !record.get("source").isEmpty()) {
                        ticket.setSource(TicketSource.valueOf(record.get("source").toUpperCase()));
                    }

                    if (record.isMapped("browser") && !record.get("browser").isEmpty()) {
                        ticket.setBrowser(record.get("browser"));
                    }

                    if (record.isMapped("device_type") && !record.get("device_type").isEmpty()) {
                        ticket.setDeviceType(DeviceType.valueOf(record.get("device_type").toUpperCase()));
                    }

                    tickets.add(ticket);
                } catch (Exception e) {
                    throw new FileParseException("Error parsing CSV record at line " + record.getRecordNumber() + ": " + e.getMessage(), e);
                }
            }
        } catch (IOException e) {
            throw new FileParseException("Error reading CSV file: " + e.getMessage(), e);
        }

        return tickets;
    }

    @Override
    public String getSupportedFormat() {
        return "csv";
    }
}
