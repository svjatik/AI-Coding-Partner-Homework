package com.workshop.ticketsystem.parser;

import com.workshop.ticketsystem.dto.CreateTicketRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileParser {

    List<CreateTicketRequest> parse(MultipartFile file) throws IOException;

    String getSupportedFormat();
}
