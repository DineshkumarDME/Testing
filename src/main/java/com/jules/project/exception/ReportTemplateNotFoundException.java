package com.jules.project.exception;

public class ReportTemplateNotFoundException extends RuntimeException {

    public ReportTemplateNotFoundException(String message) {
        super(message);
    }

    public ReportTemplateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
