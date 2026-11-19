package com.medchart.ehr.controller;

import com.medchart.ehr.audit.AuditAction;
import com.medchart.ehr.audit.AuditEvent;
import com.medchart.ehr.audit.AuditService;
import com.medchart.ehr.config.JwtTokenProvider;
import com.medchart.ehr.domain.auth.User;
import com.medchart.ehr.legacy.EncounterExportService;
import com.medchart.ehr.legacy.ReportGenerator;
import com.medchart.ehr.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "medchart.security.jwt.secret="
        + "test-only-hs512-signing-key-0123456789abcdefghijklmnopqrstuvwxyz-0123456789")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LegacyExportControllerSecurityTest {

    private static final String[] EXPORT_URLS = {
        "/v1/export/encounters?startDate=2024-01-01&endDate=2024-01-31",
        "/v1/export/patient/1/encounters",
        "/v1/export/reports/patient-roster",
        "/v1/export/reports/encounter-summary?startDate=2024-01-01T00:00:00&endDate=2024-01-31T00:00:00",
        "/v1/export/reports/daily"
    };

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @MockBean
    private EncounterExportService encounterExportService;

    @MockBean
    private ReportGenerator reportGenerator;

    @MockBean
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        when(encounterExportService.exportEncountersForDateRange(any(), any())).thenReturn(new byte[0]);
        when(encounterExportService.exportPatientEncounterHistory(any())).thenReturn(new byte[0]);
        when(reportGenerator.generatePatientRoster()).thenReturn("roster.csv");
        when(reportGenerator.generateEncounterSummary(any(), any())).thenReturn("summary.txt");
        when(reportGenerator.generateDailyReport()).thenReturn(new byte[0]);
        ensureUser("export-admin", User.Role.ADMIN);
        ensureUser("export-staff", User.Role.STAFF);
        ensureUser("export-provider", User.Role.PROVIDER);
    }

    @Test
    void anonymousCallersGet401() throws Exception {
        for (String url : EXPORT_URLS) {
            mockMvc.perform(get(url)).andExpect(status().isUnauthorized());
        }
        verifyNoInteractions(encounterExportService, reportGenerator);
    }

    @Test
    void invalidTokenGets401() throws Exception {
        for (String url : EXPORT_URLS) {
            mockMvc.perform(get(url).header("Authorization", "Bearer not-a-real-token"))
                    .andExpect(status().isUnauthorized());
        }
        verifyNoInteractions(encounterExportService, reportGenerator);
    }

    @ParameterizedTest
    @ValueSource(strings = {"export-staff", "export-provider"})
    void nonAdminRolesGet403(String username) throws Exception {
        String token = jwtTokenProvider.generateTokenFromUsername(username);
        for (String url : EXPORT_URLS) {
            mockMvc.perform(get(url).header("Authorization", "Bearer " + token))
                    .andExpect(status().isForbidden());
        }
        verifyNoInteractions(encounterExportService, reportGenerator);
    }

    @Test
    void adminCanExportAndEveryExportIsAuditedWithRealUser() throws Exception {
        String token = jwtTokenProvider.generateTokenFromUsername("export-admin");
        for (String url : EXPORT_URLS) {
            mockMvc.perform(get(url).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditService, times(EXPORT_URLS.length)).saveAuditEventAsync(captor.capture());
        assertThat(captor.getAllValues()).allSatisfy(event -> {
            assertThat(event.getUserId()).isEqualTo("export-admin");
            assertThat(event.getAction()).isEqualTo(AuditAction.EXPORT);
            assertThat(event.getSuccess()).isTrue();
        });
    }

    @Test
    void dateRangeIsCapped() throws Exception {
        String token = jwtTokenProvider.generateTokenFromUsername("export-admin");
        mockMvc.perform(get("/v1/export/encounters?startDate=2024-01-01&endDate=2024-12-31")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/v1/export/encounters?startDate=2024-02-01&endDate=2024-01-01")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/v1/export/reports/encounter-summary"
                        + "?startDate=2020-01-01T00:00:00&endDate=2024-01-01T00:00:00")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
        verify(encounterExportService, never()).exportEncountersForDateRange(any(), any());
        verify(reportGenerator, never()).generateEncounterSummary(any(), any());
    }

    private void ensureUser(String username, User.Role role) {
        if (userRepository.findByUsername(username).isPresent()) {
            return;
        }
        userRepository.save(User.builder()
                .username(username)
                .password("unused")
                .email(username + "@example.test")
                .firstName("Test")
                .lastName(username)
                .roles(Set.of(role))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build());
    }
}
