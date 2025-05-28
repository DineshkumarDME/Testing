package com.jules.project.service;

import com.jules.project.dto.ReportRequest;

public interface ReportService {
    byte[] generateReport(ReportRequest request, String authenticatedUserId) throws Exception;
}
