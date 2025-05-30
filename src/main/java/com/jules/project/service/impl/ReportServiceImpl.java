package com.jules.project.service.impl;

import com.jules.project.dto.ReportRequest;
import com.jules.project.exception.ReportTemplateNotFoundException;
import com.jules.project.service.ReportService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
// import net.sf.jasperreports.export.SimpleWriterExporterOutput; // Kept for reference, using SimpleOutputStreamExporterOutput for CSV for now
// Potentially configuration classes if used:
// import net.sf.jasperreports.export.SimpleXlsReportConfiguration;
// import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
// import net.sf.jasperreports.export.SimpleCsvExporterConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    private final DataSource dataSource;
    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    public ReportServiceImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public byte[] generateReport(ReportRequest request, String authenticatedUserId) throws Exception {
        String reportPath = "/reports/" + request.getReportName() + ".jrxml";
        InputStream reportStream = getClass().getResourceAsStream(reportPath);

        if (reportStream == null) {
            log.error("Report template not found at path: {} for user: {}", reportPath, authenticatedUserId);
            throw new ReportTemplateNotFoundException("Report template not found at path: " + reportPath);
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("REPORT_TITLE", "Dynamic Report Title for " + request.getReportName());
        parameters.put("userId", authenticatedUserId);
        // Add any other parameters from request.getReportParameters() if available
        // if (request.getReportParameters() != null) {
        //     parameters.putAll(request.getReportParameters());
        // }


        try (Connection connection = dataSource.getConnection();
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);
            String reportType = request.getReportType().toUpperCase();

            switch (reportType) {
                case "PDF":
                    JasperExportManager.exportReportToPdfStream(jasperPrint, byteArrayOutputStream);
                    break;
                case "XLS":
                    JRXlsExporter xlsExporter = new JRXlsExporter();
                    xlsExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    xlsExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(byteArrayOutputStream));
                    // Optional: Set XLS specific parameters if needed
                    // SimpleXlsReportConfiguration xlsReportConfiguration = new SimpleXlsReportConfiguration();
                    // xlsReportConfiguration.setOnePagePerSheet(false);
                    // xlsExporter.setConfiguration(xlsReportConfiguration);
                    xlsExporter.exportReport();
                    break;
                case "XLSX":
                    JRXlsxExporter xlsxExporter = new JRXlsxExporter();
                    xlsxExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    xlsxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(byteArrayOutputStream));
                    // Optional: Set XLSX specific parameters
                    // SimpleXlsxReportConfiguration xlsxReportConfiguration = new SimpleXlsxReportConfiguration();
                    // xlsxReportConfiguration.setOnePagePerSheet(false);
                    // xlsxExporter.setConfiguration(xlsxReportConfiguration);
                    xlsxExporter.exportReport();
                    break;
                case "CSV":
                    JRCsvExporter csvExporter = new JRCsvExporter();
                    csvExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    // Using SimpleOutputStreamExporterOutput for CSV. If output is not as expected,
                    // may need to use SimpleWriterExporterOutput with a java.io.Writer (e.g., OutputStreamWriter).
                    csvExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(byteArrayOutputStream));
                    // Optional: Set CSV specific parameters
                    // SimpleCsvExporterConfiguration csvConfiguration = new SimpleCsvExporterConfiguration();
                    // csvExporter.setConfiguration(csvConfiguration);
                    csvExporter.exportReport();
                    break;
                default:
                    // Connection is managed by try-with-resources, no need to close explicitly here
                    log.warn("Unsupported report type: {} requested by user: {}", request.getReportType(), authenticatedUserId);
                    throw new IllegalArgumentException("Unsupported report type: " + request.getReportType());
            }
            return byteArrayOutputStream.toByteArray();

        } catch (SQLException e) {
            log.error("SQL error during report generation for report: {}, userId: {}", request.getReportName(), authenticatedUserId, e);
            throw new RuntimeException("Database error during report generation", e);
        } catch (JRException e) {
            log.error("JasperReports error for report: {}, userId: {}", request.getReportName(), authenticatedUserId, e);
            throw new RuntimeException("Reporting engine error: " + e.getMessage(), e);
        } catch (Exception e) { // Catch any other unexpected errors
            log.error("Unexpected error during report generation for report: {}, userId: {}", request.getReportName(), authenticatedUserId, e);
            throw new RuntimeException("Unexpected error generating report: " + e.getMessage(), e);
        }
        // The original finally block for closing connection is no longer needed due to try-with-resources.
    }
}
