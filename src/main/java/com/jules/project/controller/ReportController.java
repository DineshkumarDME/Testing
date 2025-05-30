package com.jules.project.controller;

import com.jules.project.dto.ReportRequest;
import com.jules.project.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.jules.project.aop.annotation.Auditable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;
    private static final Logger log = LoggerFactory.getLogger(ReportController.class);

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Auditable
    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateReport(@RequestBody ReportRequest reportRequest, @AuthenticationPrincipal Jwt jwt) throws Exception { // Consider changing throws Exception if specific exceptions are known
        String authenticatedUserId = jwt.getSubject();
        if (authenticatedUserId == null) {
            // This check might be better placed in the service or handled by security configs if a JWT is always expected to have a subject.
            log.error("User ID (subject) could not be determined from JWT for report generation. JWT claims: {}", jwt.getClaims());
            throw new IllegalStateException("User ID (subject) could not be determined from JWT.");
        }

        // Note: reportService.generateReport can throw IllegalArgumentException for unsupported types,
        // ReportTemplateNotFoundException, RuntimeException for SQL/JRE/unexpected errors.
        byte[] reportBytes = reportService.generateReport(reportRequest, authenticatedUserId);

        HttpHeaders headers = new HttpHeaders();
        String reportTypeUp = reportRequest.getReportType().toUpperCase(); // Perform toUpperCase once
        String filename = reportRequest.getReportName();
        MediaType contentType;
        String fileExtension;

        switch (reportTypeUp) {
            case "PDF":
                contentType = MediaType.APPLICATION_PDF;
                fileExtension = ".pdf";
                break;
            case "XLS":
                contentType = MediaType.valueOf("application/vnd.ms-excel");
                fileExtension = ".xls";
                break;
            case "XLSX":
                contentType = MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                fileExtension = ".xlsx";
                break;
            case "CSV":
                contentType = MediaType.valueOf("text/csv");
                fileExtension = ".csv";
                break;
            default:
                // This path will be taken if reportRequest.getReportType() is an unknown type.
                // ReportServiceImpl would have already thrown an IllegalArgumentException.
                // If somehow it didn't (e.g. null reportType before service call, though unlikely with DTO validation),
                // this provides a fallback.
                // A global exception handler (@ControllerAdvice) is the best place to map such exceptions to HTTP 400.
                log.warn("Reached default case for report type in controller: {}. This might indicate an issue if service didn't validate.", reportRequest.getReportType());
                throw new IllegalArgumentException("Unsupported report type provided: " + reportRequest.getReportType());
        }

        headers.setContentType(contentType);
        headers.setContentDispositionFormData("filename", filename + fileExtension);

        return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
    }
}
