package com.jules.project.service.impl;

import com.jules.project.dto.ReportRequest;
import com.jules.project.exception.ReportTemplateNotFoundException;
import com.jules.project.service.ReportService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Override
    public byte[] generateReport(ReportRequest request, String authenticatedUserId) throws Exception {
        String reportPath = "/reports/" + request.getReportName() + ".jrxml";
        InputStream reportStream = getClass().getResourceAsStream(reportPath);

        if (reportStream == null) {
            throw new ReportTemplateNotFoundException("Report template not found at path: " + reportPath);
        }

        JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

        // For now, using an empty data source and parameters
        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(null); // Or new JREmptyDataSource();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("REPORT_TITLE", "Dynamic Report Title for " + request.getReportName());
        parameters.put("userId", authenticatedUserId); // Add authenticated user ID

        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        return JasperExportManager.exportReportToPdf(jasperPrint);
    }
}
