package com.jules.project.service.impl;

import com.jules.project.dto.ReportRequest;
import com.jules.project.dto.ReportOutput;
import com.jules.project.dto.ReportRequest;
import com.jules.project.exception.ReportTemplateNotFoundException;
import com.jules.project.service.ReportService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
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

    public ReportServiceImpl(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public ReportOutput generateReport(ReportRequest request, String authenticatedUserId) throws Exception {
        String reportPath = "/reports/" + request.getReportName() + ".jrxml";
        InputStream reportStream = getClass().getResourceAsStream(reportPath);


		if (reportStream == null) {
			throw new ReportTemplateNotFoundException("Report template not found at path: " + reportPath);
		}

		JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("REPORT_TITLE", "Dynamic Report Title for " + request.getReportName());
        parameters.put("userId", "admin");

        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

            byte[] reportBytes;
            String contentType;
            String fileExtension;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            switch (request.getReportType().toLowerCase()) {
                case "pdf":
                    reportBytes = JasperExportManager.exportReportToPdf(jasperPrint);
                    contentType = "application/pdf";
                    fileExtension = ".pdf";
                    break;
                case "csv":
                    JRCsvExporter csvExporter = new JRCsvExporter();
                    csvExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    csvExporter.setExporterOutput(new SimpleWriterExporterOutput(outputStream));
                    csvExporter.exportReport();
                    reportBytes = outputStream.toByteArray();
                    contentType = "text/csv";
                    fileExtension = ".csv";
                    break;
                case "xls":
                    JRXlsExporter xlsExporter = new JRXlsExporter();
                    xlsExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    xlsExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
                    xlsExporter.exportReport();
                    reportBytes = outputStream.toByteArray();
                    contentType = "application/vnd.ms-excel";
                    fileExtension = ".xls";
                    break;
                case "xlsx":
                    JRXlsxExporter xlsxExporter = new JRXlsxExporter();
                    xlsxExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                    xlsxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(outputStream));
                    xlsxExporter.exportReport();
                    reportBytes = outputStream.toByteArray();
                    contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
                    fileExtension = ".xlsx";
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported report type: " + request.getReportType());
            }
            return new ReportOutput(reportBytes, contentType, fileExtension);
        } catch (SQLException e) {
            throw new RuntimeException("Error obtaining/using JDBC connection for report generation", e);
        } catch (JRException e) {
            throw new RuntimeException("Error generating JasperReport: " + e.getMessage(), e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    System.err.println("Failed to close JDBC connection: " + e.getMessage());
                }
            }
        }
    }
}
