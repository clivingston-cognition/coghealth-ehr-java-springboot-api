package com.medchart.ehr.config;

import com.medchart.ehr.audit.AuditAction;
import com.medchart.ehr.audit.AuditEvent;
import com.medchart.ehr.audit.AuditEventRepository;
import com.medchart.ehr.domain.auth.User;
import com.medchart.ehr.dto.PatientDTO;
import com.medchart.ehr.legacy.EncounterExportService;
import com.medchart.ehr.legacy.ReportGenerator;
import com.medchart.ehr.repository.UserRepository;
import com.medchart.ehr.service.PatientService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "medchart.security.jwt.secret="
        + "test-only-jwt-signing-secret-that-is-at-least-sixty-four-bytes-long-for-hs512")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    private static final String PASSWORD = "Sup3r-Secret!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private PatientService patientService;

    @MockBean
    private EncounterExportService encounterExportService;

    @MockBean
    private ReportGenerator reportGenerator;

    @BeforeEach
    void setUp() {
        createUser("admin-user", User.Role.ADMIN);
        createUser("staff-user", User.Role.STAFF);
        createUser("provider-user", User.Role.PROVIDER);

        when(patientService.getPatientById(anyLong())).thenReturn(PatientDTO.builder().id(1L).build());
        when(encounterExportService.exportPatientEncounterHistory(anyLong())).thenReturn("export".getBytes());
        when(reportGenerator.generateDailyReport()).thenReturn("report".getBytes());
    }

    @Test
    void patientRouteRequiresToken() throws Exception {
        mockMvc.perform(get("/v1/patients/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(withToken(get("/v1/patients/1"), "not-a-jwt")).andExpect(status().isUnauthorized());
        mockMvc.perform(withToken(get("/v1/patients/1"), tokenProvider.generateTokenFromUsername("ghost")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patientRouteAllowsClinicalUser() throws Exception {
        mockMvc.perform(asUser(get("/v1/patients/1"), "provider-user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void patientWritesRequireRegistrationRole() throws Exception {
        String body = "{\"mrn\":\"MRN1\",\"firstName\":\"A\",\"lastName\":\"B\"}";
        mockMvc.perform(asUser(post("/v1/patients"), "provider-user")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
        mockMvc.perform(asUser(put("/v1/patients/1"), "provider-user")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void exportRoutesRequireToken() throws Exception {
        mockMvc.perform(get("/v1/export/patient/1/encounters")).andExpect(status().isUnauthorized());
        mockMvc.perform(withToken(get("/v1/export/patient/1/encounters"), "not-a-jwt"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/v1/export/reports/daily")).andExpect(status().isUnauthorized());
    }

    @Test
    void exportRoutesRejectNonAdminRoles() throws Exception {
        mockMvc.perform(asUser(get("/v1/export/patient/1/encounters"), "staff-user"))
                .andExpect(status().isForbidden());
        mockMvc.perform(asUser(get("/v1/export/patient/1/encounters"), "provider-user"))
                .andExpect(status().isForbidden());
        mockMvc.perform(asUser(get("/v1/export/reports/daily"), "staff-user"))
                .andExpect(status().isForbidden());
    }

    @Test
    void exportRoutesAllowAdmin() throws Exception {
        mockMvc.perform(asUser(get("/v1/export/patient/1/encounters"), "admin-user"))
                .andExpect(status().isOk())
                .andExpect(content().string("export"));
        mockMvc.perform(asUser(get("/v1/export/reports/daily"), "admin-user"))
                .andExpect(status().isOk());
    }

    @Test
    void encounterRoutesRequireToken() throws Exception {
        mockMvc.perform(get("/v1/encounters/1")).andExpect(status().isUnauthorized());
        mockMvc.perform(post("/v1/encounters/1/cancel")).andExpect(status().isUnauthorized());
        mockMvc.perform(withToken(post("/v1/encounters/1/complete"), "not-a-jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void encounterStatusChangesAreRoleRestricted() throws Exception {
        mockMvc.perform(asUser(post("/v1/encounters/1/cancel"), "provider-user")).andExpect(status().isForbidden());
        mockMvc.perform(asUser(post("/v1/encounters/1/complete"), "staff-user")).andExpect(status().isForbidden());
        mockMvc.perform(asUser(post("/v1/encounters/1/complete"), "provider-user")).andExpect(status().isOk());
        mockMvc.perform(asUser(get("/v1/encounters/1"), "staff-user")).andExpect(status().isNotFound());
    }

    @Test
    void encounterAccessIsAuditedWithCallerId() throws Exception {
        Long staffId = userRepository.findByUsername("staff-user").orElseThrow().getId();
        mockMvc.perform(asUser(post("/v1/encounters/987654/cancel"), "staff-user")).andExpect(status().isOk());

        AuditEvent event = null;
        for (int i = 0; i < 50 && event == null; i++) {
            event = auditEventRepository.findAll().stream()
                    .filter(e -> e.getAction() == AuditAction.UPDATE
                            && Long.valueOf(987654L).equals(e.getResourceId()))
                    .findFirst().orElse(null);
            if (event == null) {
                Thread.sleep(100);
            }
        }
        assertThat(event).isNotNull();
        assertThat(event.getUserId()).isEqualTo(String.valueOf(staffId));
        assertThat(event.getUserName()).isEqualTo("staff-user");
        assertThat(event.getResourceType()).isEqualTo("Encounter");
    }

    @Test
    void loginIsPublicAndIssuesUsableToken() throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"staff-user\",\"password\":\"" + PASSWORD + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String token = response.replaceAll(".*\"token\"\\s*:\\s*\"([^\"]+)\".*", "$1");

        mockMvc.perform(withToken(get("/v1/patients/1"), token)).andExpect(status().isOk());
    }

    @Test
    void registrationIsNotPublic() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"mallory\",\"password\":\"x\",\"email\":\"m@example.com\","
                                + "\"firstName\":\"M\",\"lastName\":\"X\"}"))
                .andExpect(status().isUnauthorized());
    }

    private void createUser(String username, User.Role role) {
        if (userRepository.existsByUsername(username)) {
            return;
        }
        userRepository.save(User.builder()
                .username(username)
                .email(username + "@example.com")
                .firstName(username)
                .lastName("Test")
                .password(passwordEncoder.encode(PASSWORD))
                .roles(Set.of(role))
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build());
    }

    private MockHttpServletRequestBuilder asUser(MockHttpServletRequestBuilder request, String username) {
        return withToken(request, tokenProvider.generateTokenFromUsername(username));
    }

    private MockHttpServletRequestBuilder withToken(MockHttpServletRequestBuilder request, String token) {
        return request.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }
}
