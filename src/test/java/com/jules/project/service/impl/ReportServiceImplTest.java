package com.jules.project.service.impl;

import com.jules.project.dto.ReportRequest;
import com.jules.project.exception.ReportTemplateNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    private ReportServiceImpl reportService;

    @Mock
    private DataSource mockDataSource;

    @Mock
    private Connection mockConnection;

    @BeforeEach
    void setUp() { // Removed SQLException as it's not thrown here anymore
        reportService = new ReportServiceImpl(mockDataSource);
        // General setup, specific mock behaviors moved to individual tests
    }

    @Test
    void generateReport_success() throws Exception {
        // Specific mock behavior for this test case
        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        // Optional: Mock further behavior of mockConnection if needed for this test
        // when(mockConnection.getMetaData()).thenReturn(mock(java.sql.DatabaseMetaData.class));

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
