package com.workshop.ticketsystem.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workshop.ticketsystem.dto.CreateTicketRequest;
import com.workshop.ticketsystem.dto.UpdateTicketRequest;
import com.workshop.ticketsystem.enums.TicketCategory;
import com.workshop.ticketsystem.enums.TicketPriority;
import com.workshop.ticketsystem.enums.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateTicketSuccess() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerId("C001");
        request.setCustomerEmail("test@example.com");
        request.setCustomerName("Test User");
        request.setSubject("Test Subject");
        request.setDescription("This is a test ticket description for testing purposes.");

        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.customerId").value("C001"))
                .andExpect(jsonPath("$.customerEmail").value("test@example.com"))
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    @Test
    void testCreateTicketValidationFailure() throws Exception {
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerId("C001");
        request.setCustomerEmail("invalid-email");
        request.setCustomerName("Test User");
        request.setSubject("Test");
        request.setDescription("Short");

        mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Error"));
    }

    @Test
    void testGetAllTickets() throws Exception {
        // Create a ticket first
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerId("C001");
        request.setCustomerEmail("test@example.com");
        request.setCustomerName("Test User");
        request.setSubject("Test Subject");
        request.setDescription("This is a test ticket description.");

        mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Get all tickets
        mockMvc.perform(get("/tickets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void testGetTicketsWithFilters() throws Exception {
        // Create a ticket with specific category
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerId("C001");
        request.setCustomerEmail("test@example.com");
        request.setCustomerName("Test User");
        request.setSubject("Test Subject");
        request.setDescription("This is a test ticket description.");
        request.setCategory(TicketCategory.BUG_REPORT);
        request.setPriority(TicketPriority.HIGH);

        mockMvc.perform(post("/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Filter by category
        mockMvc.perform(get("/tickets")
                        .param("category", "BUG_REPORT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].category").value("BUG_REPORT"));
    }

    @Test
    void testGetTicketByIdFound() throws Exception {
        // Create a ticket
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerId("C001");
        request.setCustomerEmail("test@example.com");
        request.setCustomerName("Test User");
        request.setSubject("Test Subject");
        request.setDescription("This is a test ticket description.");

        String response = mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        // Get ticket by ID
        mockMvc.perform(get("/tickets/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.customerId").value("C001"));
    }

    @Test
    void testGetTicketByIdNotFound() throws Exception {
        mockMvc.perform(get("/tickets/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    void testUpdateTicket() throws Exception {
        // Create a ticket
        CreateTicketRequest createRequest = new CreateTicketRequest();
        createRequest.setCustomerId("C001");
        createRequest.setCustomerEmail("test@example.com");
        createRequest.setCustomerName("Test User");
        createRequest.setSubject("Original Subject");
        createRequest.setDescription("Original description for testing.");

        String response = mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        // Update ticket
        UpdateTicketRequest updateRequest = new UpdateTicketRequest();
        updateRequest.setSubject("Updated Subject");
        updateRequest.setStatus(TicketStatus.IN_PROGRESS);

        mockMvc.perform(put("/tickets/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("Updated Subject"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void testDeleteTicket() throws Exception {
        // Create a ticket
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerId("C001");
        request.setCustomerEmail("test@example.com");
        request.setCustomerName("Test User");
        request.setSubject("Test Subject");
        request.setDescription("This is a test ticket description.");

        String response = mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        // Delete ticket
        mockMvc.perform(delete("/tickets/" + id))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/tickets/" + id))
                .andExpect(status().isNotFound());
    }

    @Test
    void testImportCsv() throws Exception {
        String csvContent = "customer_id,customer_email,customer_name,subject,description\n" +
                "C001,test1@example.com,Test User 1,Test Subject 1,This is a test ticket description for import testing.\n" +
                "C002,test2@example.com,Test User 2,Test Subject 2,This is another test ticket description for testing.";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.csv",
                "text/csv",
                csvContent.getBytes()
        );

        mockMvc.perform(multipart("/tickets/import")
                        .file(file)
                        .param("format", "csv")
                        .param("autoClassify", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords").value(2))
                .andExpect(jsonPath("$.successfulImports").value(2))
                .andExpect(jsonPath("$.failedImports").value(0));
    }

    @Test
    void testImportJson() throws Exception {
        String jsonContent = "[{\"customerId\":\"C001\",\"customerEmail\":\"test@example.com\"," +
                "\"customerName\":\"Test User\",\"subject\":\"Test Subject\"," +
                "\"description\":\"This is a test ticket description for JSON import testing.\"}]";

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.json",
                "application/json",
                jsonContent.getBytes()
        );

        mockMvc.perform(multipart("/tickets/import")
                        .file(file)
                        .param("format", "json")
                        .param("autoClassify", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRecords").value(1))
                .andExpect(jsonPath("$.successfulImports").value(1));
    }

    @Test
    void testAutoClassify() throws Exception {
        // Create a ticket with descriptive content
        CreateTicketRequest request = new CreateTicketRequest();
        request.setCustomerId("C001");
        request.setCustomerEmail("test@example.com");
        request.setCustomerName("Test User");
        request.setSubject("Cannot login to my account");
        request.setDescription("I forgot my password and cannot reset it. The reset link is not working.");

        String response = mockMvc.perform(post("/tickets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        String id = objectMapper.readTree(response).get("id").asText();

        // Auto-classify
        mockMvc.perform(post("/tickets/" + id + "/auto-classify"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").exists())
                .andExpect(jsonPath("$.priority").exists())
                .andExpect(jsonPath("$.confidenceScore").exists())
                .andExpect(jsonPath("$.reasoning").exists());
    }
}
