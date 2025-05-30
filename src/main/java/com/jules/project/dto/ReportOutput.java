package com.jules.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ReportOutput {
    private byte[] reportBytes;
    private String contentType;
    private String fileExtension;
}
