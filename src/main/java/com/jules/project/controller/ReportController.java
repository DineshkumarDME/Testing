package com.jules.project.controller;

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

        byte[] reportBytes = reportService.generateReport(reportRequest, authenticatedUserId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", reportRequest.getReportName() + ".pdf"); 

        return new ResponseEntity<>(reportBytes, headers, HttpStatus.OK);
    }
}
