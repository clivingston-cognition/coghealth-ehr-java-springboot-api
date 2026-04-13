package com.medchart.ehr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchPatientResult {
    private int totalRequested;
    private int successCount;
    private int failureCount;
    private List<PatientDTO> created;
    private List<BatchItemError> errors;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BatchItemError {
        private int index;
        private PatientDTO input;
        private String errorMessage;
    }
}
