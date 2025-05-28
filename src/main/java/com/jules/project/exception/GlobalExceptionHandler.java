package com.jules.project.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReportTemplateNotFoundException.class)
    public ResponseEntity<String> handleReportTemplateNotFoundException(ReportTemplateNotFoundException ex) {
        return new ResponseEntity<>("Report template not found: " + ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(Exception ex) {
        // Log the exception here for debugging purposes if a logger is configured
        System.err.println("An unexpected error occurred: " + ex.getMessage()); // Basic logging
        ex.printStackTrace(); // For more detailed debugging, consider a proper logging framework
        return new ResponseEntity<>("An unexpected error occurred while generating the report. Please contact support.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
