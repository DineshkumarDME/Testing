package com.jules.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReportOutput {
    private byte[] reportBytes;
    private String contentType;
    private String fileExtension;
	public byte[] getReportBytes() {
		return reportBytes;
	}
	public void setReportBytes(byte[] reportBytes) {
		this.reportBytes = reportBytes;
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getFileExtension() {
		return fileExtension;
	}
	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}
	
	public ReportOutput(byte[] reportBytes, String contentType, String fileExtension) {
		super();
		this.reportBytes = reportBytes;
		this.contentType = contentType;
		this.fileExtension = fileExtension;
	}
	
	public ReportOutput() {
		super();
	}
    
    
}
