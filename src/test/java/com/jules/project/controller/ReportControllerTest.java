package com.jules.project.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jules.project.dto.ReportRequest;
import com.jules.project.exception.ReportTemplateNotFoundException;
import com.jules.project.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void generateReport_success() throws Exception {
        ReportRequest request = new ReportRequest("PDF", "user123", "test_report");
        byte[] pdfBytes = "Sample PDF Content".getBytes();

        when(reportService.generateReport(any(ReportRequest.class), anyString())).thenReturn(pdfBytes);

        mockMvc.perform(post("/api/reports/generate")
                .with(jwt()) // Add this to mock a JWT principal
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PDF_VALUE))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"filename\"; filename=\"test_report.pdf\""))
                .andExpect(content().bytes(pdfBytes));
    }

    @Test
    void generateReport_templateNotFound() throws Exception {
        ReportRequest request = new ReportRequest("PDF", "user123", "non_existent_report");

        when(reportService.generateReport(any(ReportRequest.class), anyString()))
                .thenThrow(new ReportTemplateNotFoundException("Template not found"));

        mockMvc.perform(post("/api/reports/generate")
                .with(jwt()) // Add this to mock a JWT principal
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void generateReport_successXLS() throws Exception {
        ReportRequest request = new ReportRequest("XLS", "user123", "xls_report");
        byte[] xlsBytes = "Sample XLS Content".getBytes(); // Dummy content

        when(reportService.generateReport(any(ReportRequest.class), anyString())).thenReturn(xlsBytes);

        mockMvc.perform(post("/api/reports/generate")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.ms-excel"))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"filename\"; filename=\"xls_report.xls\""))
                .andExpect(content().bytes(xlsBytes));
    }

    @Test
    void generateReport_successXLSX() throws Exception {
        ReportRequest request = new ReportRequest("XLSX", "user123", "xlsx_report");
        byte[] xlsxBytes = "Sample XLSX Content".getBytes(); // Dummy content

        when(reportService.generateReport(any(ReportRequest.class), anyString())).thenReturn(xlsxBytes);

        mockMvc.perform(post("/api/reports/generate")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"filename\"; filename=\"xlsx_report.xlsx\""))
                .andExpect(content().bytes(xlsxBytes));
    }

    @Test
    void generateReport_successCSV() throws Exception {
        ReportRequest request = new ReportRequest("CSV", "user123", "csv_report");
        byte[] csvBytes = "Sample CSV,Content".getBytes(); // Dummy content

        when(reportService.generateReport(any(ReportRequest.class), anyString())).thenReturn(csvBytes);

        mockMvc.perform(post("/api/reports/generate")
                .with(jwt())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", "text/csv"))
                .andExpect(header().string("Content-Disposition", "form-data; name=\"filename\"; filename=\"csv_report.csv\""))
                .andExpect(content().bytes(csvBytes));
    }
}
