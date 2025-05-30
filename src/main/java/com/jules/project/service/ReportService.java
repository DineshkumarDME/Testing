package com.jules.project.service;

import com.jules.project.dto.ReportOutput;
import com.jules.project.dto.ReportRequest;

public interface ReportService {
    ReportOutput generateReport(ReportRequest request, String authenticatedUserId) throws Exception;
}
