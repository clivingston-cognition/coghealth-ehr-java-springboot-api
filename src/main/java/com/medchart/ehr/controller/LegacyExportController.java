package com.medchart.ehr.controller;

import com.medchart.ehr.audit.AuditAccess;
import com.medchart.ehr.audit.AuditAction;
import com.medchart.ehr.legacy.EncounterExportService;
import com.medchart.ehr.legacy.ReportGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/v1/export")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class LegacyExportController {

    static final int MAX_EXPORT_RANGE_DAYS = 31;

    private final EncounterExportService encounterExportService;
    private final ReportGenerator reportGenerator;

    @GetMapping("/encounters")
    @AuditAccess(action = AuditAction.EXPORT, resourceType = "Encounter", description = "Export encounters for date range")
    public ResponseEntity<byte[]> exportEncounters(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        validateRange(startDate.atStartOfDay(), endDate.atStartOfDay());
        
        byte[] data = encounterExportService.exportEncountersForDateRange(startDate, endDate);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=encounters_export.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    @GetMapping("/patient/{patientId}/encounters")
    @AuditAccess(action = AuditAction.EXPORT, resourceType = "Patient", description = "Export patient encounter history")
    public ResponseEntity<byte[]> exportPatientEncounters(@PathVariable Long patientId) {
        byte[] data = encounterExportService.exportPatientEncounterHistory(patientId);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=patient_encounters.txt")
                .contentType(MediaType.TEXT_PLAIN)
                .body(data);
    }

    @GetMapping("/reports/patient-roster")
    @AuditAccess(action = AuditAction.EXPORT, resourceType = "Patient", description = "Generate patient roster report")
    public ResponseEntity<String> generatePatientRoster() {
        String filePath = reportGenerator.generatePatientRoster();
        return ResponseEntity.ok("Report generated at: " + filePath);
    }

    @GetMapping("/reports/encounter-summary")
    @AuditAccess(action = AuditAction.EXPORT, resourceType = "Encounter", description = "Generate encounter summary report")
    public ResponseEntity<String> generateEncounterSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        validateRange(startDate, endDate);
        
        String filePath = reportGenerator.generateEncounterSummary(startDate, endDate);
        return ResponseEntity.ok("Report generated at: " + filePath);
    }

    @GetMapping("/reports/daily")
    @AuditAccess(action = AuditAction.EXPORT, resourceType = "Patient", description = "Download daily patient roster report")
    public ResponseEntity<byte[]> getDailyReport() {
        byte[] data = reportGenerator.generateDailyReport();
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=daily_report.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }

    private void validateRange(LocalDateTime start, LocalDateTime end) {
        if (end.isBefore(start)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "endDate must not be before startDate");
        }
        if (start.plusDays(MAX_EXPORT_RANGE_DAYS).isBefore(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Export date range must not exceed " + MAX_EXPORT_RANGE_DAYS + " days");
        }
    }
}
