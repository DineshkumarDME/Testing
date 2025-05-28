package com.jules.project.service.impl;

import com.jules.project.dto.ReportRequest;
import com.jules.project.exception.ReportTemplateNotFoundException;
import com.jules.project.service.ReportService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import javax.sql.DataSource; // Import DataSource
import java.io.InputStream;
import java.sql.Connection; // Import Connection
import java.sql.SQLException; // Import SQLException
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    private final DataSource dataSource; // Add DataSource field

    // Constructor injection for DataSource
    public ReportServiceImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public byte[] generateReport(ReportRequest request, String authenticatedUserId) throws Exception {
        String reportPath = "/reports/" + request.getReportName() + ".jrxml";
        InputStream reportStream = getClass().getResourceAsStream(reportPath);

        if (reportStream == null) {
            throw new ReportTemplateNotFoundException("Report template not found at path: " + reportPath);
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("userId", "admin"); // Add authenticated user ID

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
            return JasperExportManager.exportReportToPdf(jasperPrint);
        } catch (SQLException e) {
            // Handle SQLException appropriately, e.g., log it and/or rethrow as a custom exception
            throw new RuntimeException("Error obtaining/using JDBC connection for report generation", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    // Log error or handle as appropriate, e.g., using a logger
                    System.err.println("Failed to close JDBC connection: " + e.getMessage());
                }
            }
        }
    }
}
