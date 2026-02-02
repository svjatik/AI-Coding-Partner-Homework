package com.workshop.ticketsystem.service;

import com.workshop.ticketsystem.dto.ImportSummaryResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ImportService {

    ImportSummaryResponse importTickets(MultipartFile file, String format, boolean autoClassify);
}
