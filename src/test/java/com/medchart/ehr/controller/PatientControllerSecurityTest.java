package com.medchart.ehr.controller;

import com.medchart.ehr.audit.AuditEvent;
import com.medchart.ehr.audit.AuditService;
import com.medchart.ehr.config.JwtTokenProvider;
import com.medchart.ehr.domain.auth.User;
import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.repository.PatientRepository;
import com.medchart.ehr.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "medchart.security.jwt.secret="
        + "test-only-hs512-signing-key-0123456789abcdefghijklmnopqrstuvwxyz-0123456789")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PatientControllerSecurityTest {

    private static final String UPDATE_BODY = "{\"firstName\":\"Jane\",\"lastName\":\"Patient\","
            + "\"phoneMobile\":\"555-0199\",\"mrn\":\"MRN-HIJACK\",\"active\":false,\"deceased\":true}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @MockBean
    private AuditService auditService;

    private Patient patient;

    @BeforeEach
    void setUp() {
        ensureUser("patient-admin", User.Role.ADMIN);
        ensureUser("patient-staff", User.Role.STAFF);
        patientRepository.deleteAll();
        patient = patientRepository.save(newPatient("MRN-SEC-1", "Jane", "Patient"));
        patientRepository.save(newPatient("MRN-SEC-2", "John", "Doe"));
        patientRepository.save(newPatient("MRN-SEC-3", "Ann", "100%_Literal"));
    }

    private List<RequestBuilder> allPatientRoutes() {
        return List.of(
                get("/v1/patients/" + patient.getId()),
                get("/v1/patients/mrn/MRN-SEC-1"),
                get("/v1/patients/search").param("q", "Jane"),
                post("/v1/patients").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"Mal\",\"lastName\":\"Lory\",\"dateOfBirth\":\"1990-01-01\"}"),
                put("/v1/patients/" + patient.getId()).contentType(MediaType.APPLICATION_JSON)
                        .content(UPDATE_BODY));
    }

    @Test
    void anonymousCallersGet401OnEveryPatientRoute() throws Exception {
        for (RequestBuilder request : allPatientRoutes()) {
            mockMvc.perform(request).andExpect(status().isUnauthorized());
        }
        assertThat(patientRepository.count()).isEqualTo(3);
        assertThat(patientRepository.findById(patient.getId()).orElseThrow().getDeceased()).isFalse();
    }

    @Test
    void invalidTokenGets401() throws Exception {
        mockMvc.perform(get("/v1/patients/" + patient.getId())
                        .header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void anonymousCannotSelfRegister() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"mallory\",\"email\":\"m@example.test\",\"password\":\"x\","
                                + "\"firstName\":\"M\",\"lastName\":\"L\"}"))
                .andExpect(status().isUnauthorized());
        assertThat(userRepository.existsByUsername("mallory")).isFalse();
    }

    @Test
    void nonAdminCannotRegisterViaPathVariants() throws Exception {
        for (String path : new String[] {"/api/auth/register", "/api/auth/register/"}) {
            mockMvc.perform(post(path).header("Authorization", bearer("patient-staff"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"username\":\"mallory2\",\"email\":\"m2@example.test\",\"password\":\"x\","
                                    + "\"firstName\":\"M\",\"lastName\":\"L\"}"))
                    .andExpect(status().isForbidden());
        }
        assertThat(userRepository.existsByUsername("mallory2")).isFalse();
    }

    @Test
    void authenticatedStaffCanReadPatient() throws Exception {
        mockMvc.perform(get("/v1/patients/" + patient.getId()).header("Authorization", bearer("patient-staff")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mrn").value("MRN-SEC-1"));
    }

    @Test
    void searchRejectsShortOrEmptyTerms() throws Exception {
        for (String q : new String[] {"", " ", "%", "a"}) {
            mockMvc.perform(get("/v1/patients/search").param("q", q).header("Authorization", bearer("patient-staff")))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void searchTreatsWildcardsLiterally() throws Exception {
        mockMvc.perform(get("/v1/patients/search").param("q", "%%").header("Authorization", bearer("patient-staff")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
        mockMvc.perform(get("/v1/patients/search").param("q", "__").header("Authorization", bearer("patient-staff")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
        mockMvc.perform(get("/v1/patients/search").param("q", "0%_").header("Authorization", bearer("patient-staff")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].mrn").value("MRN-SEC-3"));
    }

    @Test
    void searchPageSizeIsCapped() throws Exception {
        mockMvc.perform(get("/v1/patients/search").param("q", "MRN").param("size", "2000")
                        .header("Authorization", bearer("patient-staff")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(PatientController.MAX_SEARCH_PAGE_SIZE));
    }

    @Test
    void nonAdminUpdateCannotChangeRecordStatusFields() throws Exception {
        mockMvc.perform(put("/v1/patients/" + patient.getId()).header("Authorization", bearer("patient-staff"))
                        .contentType(MediaType.APPLICATION_JSON).content(UPDATE_BODY))
                .andExpect(status().isOk());

        Patient updated = patientRepository.findById(patient.getId()).orElseThrow();
        assertThat(updated.getPhoneMobile()).isEqualTo("555-0199");
        assertThat(updated.getMrn()).isEqualTo("MRN-SEC-1");
        assertThat(updated.getActive()).isTrue();
        assertThat(updated.getDeceased()).isFalse();
    }

    @Test
    void adminUpdateCanChangeRecordStatusFields() throws Exception {
        mockMvc.perform(put("/v1/patients/" + patient.getId()).header("Authorization", bearer("patient-admin"))
                        .contentType(MediaType.APPLICATION_JSON).content(UPDATE_BODY))
                .andExpect(status().isOk());

        Patient updated = patientRepository.findById(patient.getId()).orElseThrow();
        assertThat(updated.getMrn()).isEqualTo("MRN-HIJACK");
        assertThat(updated.getActive()).isFalse();
        assertThat(updated.getDeceased()).isTrue();
    }

    @Test
    void nonAdminCreateCannotChooseMrnOrStatus() throws Exception {
        mockMvc.perform(post("/v1/patients").header("Authorization", bearer("patient-staff"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\":\"New\",\"lastName\":\"Person\",\"dateOfBirth\":\"1990-01-01\","
                                + "\"mrn\":\"MRN-CHOSEN\",\"deceased\":true,\"active\":false}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.deceased").value(false))
                .andExpect(jsonPath("$.active").value(true));
        assertThat(patientRepository.findByMrn("MRN-CHOSEN")).isEmpty();
    }

    @Test
    void concurrentNonAdminCreatesGetDistinctMrns() throws Exception {
        String body = "{\"firstName\":\"Twin\",\"lastName\":\"Person\",\"dateOfBirth\":\"1990-01-01\"}";
        String token = bearer("patient-staff");
        int requests = 20;
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(requests);
        try {
            List<Future<Integer>> results = new ArrayList<>();
            for (int i = 0; i < requests; i++) {
                results.add(pool.submit(() -> {
                    start.await();
                    return mockMvc.perform(post("/v1/patients").header("Authorization", token)
                                    .contentType(MediaType.APPLICATION_JSON).content(body))
                            .andReturn().getResponse().getStatus();
                }));
            }
            start.countDown();
            for (Future<Integer> result : results) {
                assertThat(result.get(30, TimeUnit.SECONDS)).isEqualTo(201);
            }
        } finally {
            pool.shutdownNow();
        }
        assertThat(patientRepository.findAll().stream().map(Patient::getMrn).distinct().count())
                .isEqualTo(patientRepository.count());
    }

    @Test
    void auditRecordsAuthenticatedUserAndIgnoresForwardedForHeader() throws Exception {
        mockMvc.perform(get("/v1/patients/" + patient.getId())
                        .header("Authorization", bearer("patient-staff"))
                        .header("X-Forwarded-For", "203.0.113.66"))
                .andExpect(status().isOk());

        ArgumentCaptor<AuditEvent> captor = ArgumentCaptor.forClass(AuditEvent.class);
        verify(auditService, atLeastOnce()).saveAuditEventAsync(captor.capture());
        AuditEvent event = captor.getValue();
        assertThat(event.getUserId()).isEqualTo("patient-staff");
        assertThat(event.getIpAddress()).isNotEqualTo("203.0.113.66");
    }

    private String bearer(String username) {
        return "Bearer " + jwtTokenProvider.generateTokenFromUsername(username);
    }

    private static Patient newPatient(String mrn, String firstName, String lastName) {
        return Patient.builder()
                .mrn(mrn)
                .firstName(firstName)
                .lastName(lastName)
                .dateOfBirth(LocalDate.of(1980, 1, 1))
                .build();
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
