package com.jules.project.controller;

import com.jules.project.dto.ReportOutput;
import com.jules.project.exception.ReportTemplateNotFoundException;
import com.jules.project.dto.ReportRequest;
import com.jules.project.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    
    @PostMapping("/generate")
    public ResponseEntity<byte[]> generateReport(@RequestBody ReportRequest reportRequest, @AuthenticationPrincipal Jwt jwt) throws Exception {
        String authenticatedUserId = jwt.getSubject();
        if (authenticatedUserId == null) {
            throw new IllegalStateException("User ID (subject) could not be determined from JWT.");
        }

        ReportOutput reportOutput;
        try {
            reportOutput = reportService.generateReport(reportRequest, authenticatedUserId);
        } catch (ReportTemplateNotFoundException e) {
            // Log the exception - consider using a proper logger
            System.err.println("Report template not found: " + e.getMessage());
            // Return 404 Not Found for this specific case
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null); 
        } catch (Exception e) {
            // Log the exception and rethrow with more details for better debugging
            // Consider using a logger here instead of System.err
            System.err.println("Exception occurred during report generation: " + e.getMessage());
            e.printStackTrace();
            // It's generally better to throw a more specific, custom exception or handle it gracefully
            // For now, rethrowing as a RuntimeException for clarity in debugging, leading to a 500
            throw new RuntimeException("Error during report generation: " + e.getMessage(), e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(reportOutput.getContentType()));
        headers.setContentDispositionFormData("filename", reportRequest.getReportName() + reportOutput.getFileExtension());

        return new ResponseEntity<>(reportOutput.getReportBytes(), headers, HttpStatus.OK);
    }
}
