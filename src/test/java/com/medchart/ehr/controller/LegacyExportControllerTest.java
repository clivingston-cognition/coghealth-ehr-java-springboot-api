package com.medchart.ehr.controller;

import com.medchart.ehr.config.JwtTokenProvider;
import com.medchart.ehr.legacy.EncounterExportService;
import com.medchart.ehr.legacy.ReportGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LegacyExportController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class LegacyExportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EncounterExportService encounterExportService;

    @MockBean
    private ReportGenerator reportGenerator;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    @Test
    void exportEncounters_returnsOkWithCsvContentType() throws Exception {
        byte[] csvData = "EncounterId,EncounterNumber\n1,ENC-001\n".getBytes();
        when(encounterExportService.exportEncountersForDateRange(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31)))
                .thenReturn(csvData);

        mockMvc.perform(get("/v1/export/encounters")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=encounters_export.csv"));
    }

    @Test
    void exportPatientEncounters_returnsOkWithTextPlain() throws Exception {
        byte[] data = "Patient Encounter History Report\n".getBytes();
        when(encounterExportService.exportPatientEncounterHistory(1L))
                .thenReturn(data);

        mockMvc.perform(get("/v1/export/patient/1/encounters"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/plain"))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=patient_encounters.txt"));
    }

    @Test
    void generatePatientRoster_returnsOk() throws Exception {
        when(reportGenerator.generatePatientRoster())
                .thenReturn("/tmp/patient_roster_20240115.csv");

        mockMvc.perform(get("/v1/export/reports/patient-roster"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Report generated at: /tmp/patient_roster_20240115.csv"));
    }

    @Test
    void generateEncounterSummary_returnsOk() throws Exception {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 1, 31, 23, 59, 59);
        when(reportGenerator.generateEncounterSummary(start, end))
                .thenReturn("/tmp/encounter_summary.txt");

        mockMvc.perform(get("/v1/export/reports/encounter-summary")
                        .param("startDate", "2024-01-01T00:00:00")
                        .param("endDate", "2024-01-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(content().string(
                        "Report generated at: /tmp/encounter_summary.txt"));
    }

    @Test
    void getDailyReport_returnsOkWithCsvContentType() throws Exception {
        byte[] reportData = "ID,MRN,Name\n1,MRN001,John Doe\n".getBytes();
        when(reportGenerator.generateDailyReport()).thenReturn(reportData);

        mockMvc.perform(get("/v1/export/reports/daily"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/csv"))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=daily_report.csv"));
    }
}
