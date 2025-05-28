package com.jules.project.dto;


public class ReportRequest {
    private String reportType;
    private String userId;
    private String reportName;
	public String getReportType() {
		return reportType;
	}
	public void setReportType(String reportType) {
		this.reportType = reportType;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getReportName() {
		return reportName;
	}
	public void setReportName(String reportName) {
		this.reportName = reportName;
	}
	
	
	public ReportRequest(String reportType, String userId, String reportName) {
		super();
		this.reportType = reportType;
		this.userId = userId;
		this.reportName = reportName;
	}
	
	public ReportRequest() {
		super();
	}
    
    
}
