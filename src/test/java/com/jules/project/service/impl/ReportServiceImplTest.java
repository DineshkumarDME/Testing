package com.jules.project.service.impl;

import com.jules.project.dto.ReportRequest;
import com.jules.project.exception.ReportTemplateNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        reportService = new ReportServiceImpl();
    }

    @Test
    void generateReport_success() throws Exception {
        ReportRequest request = new ReportRequest("PDF", "user123", "test_report");
        byte[] result = reportService.generateReport(request, "test-user-id");
        assertNotNull(result);
        assertTrue(result.length > 0);
        // Further checks could involve PDF content validation if needed
    }

    @Test
    void generateReport_templateNotFound() {
        ReportRequest request = new ReportRequest("PDF", "user123", "non_existent_report");
        ReportTemplateNotFoundException exception = assertThrows(
                ReportTemplateNotFoundException.class,
                () -> reportService.generateReport(request, "test-user-id")
        );
        assertEquals("Report template not found at path: /reports/non_existent_report.jrxml", exception.getMessage());
    }
}
