package com.medchart.ehr.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medchart.ehr.config.JwtTokenProvider;
import com.medchart.ehr.domain.encounter.Encounter;
import com.medchart.ehr.domain.encounter.EncounterStatus;
import com.medchart.ehr.domain.encounter.EncounterType;
import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.domain.provider.Provider;
import com.medchart.ehr.domain.provider.ProviderType;
import com.medchart.ehr.service.EncounterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EncounterController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class EncounterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EncounterService encounterService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private ObjectMapper objectMapper;
    private Encounter sampleEncounter;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        Patient patient = Patient.builder()
                .id(1L)
                .mrn("MRN001")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1985, 3, 15))
                .build();

        Provider provider = Provider.builder()
                .id(1L)
                .npi("1234567890")
                .firstName("Dr")
                .lastName("Smith")
                .providerType(ProviderType.PHYSICIAN)
                .build();

        sampleEncounter = Encounter.builder()
                .id(1L)
                .encounterNumber("ENC-2024-000001")
                .patient(patient)
                .attendingProvider(provider)
                .encounterType(EncounterType.OFFICE_VISIT)
                .status(EncounterStatus.SCHEDULED)
                .encounterDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .build();
    }

    @Test
    void getById_returnsOkWithDetails() throws Exception {
        when(encounterService.findByIdWithDetails(1L))
                .thenReturn(Optional.of(sampleEncounter));

        mockMvc.perform(get("/v1/encounters/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.encounterNumber").value("ENC-2024-000001"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    void getById_returns404WhenNotFound() throws Exception {
        when(encounterService.findByIdWithDetails(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/encounters/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByNumber_returnsOk() throws Exception {
        when(encounterService.findByEncounterNumber("ENC-2024-000001"))
                .thenReturn(Optional.of(sampleEncounter));

        mockMvc.perform(get("/v1/encounters/number/ENC-2024-000001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.encounterNumber").value("ENC-2024-000001"));
    }

    @Test
    void getByNumber_returns404WhenNotFound() throws Exception {
        when(encounterService.findByEncounterNumber("INVALID"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/encounters/number/INVALID"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByPatient_returnsOkWithList() throws Exception {
        when(encounterService.findByPatientId(1L))
                .thenReturn(List.of(sampleEncounter));

        mockMvc.perform(get("/v1/encounters/patient/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].encounterNumber").value("ENC-2024-000001"));
    }

    @Test
    void getByPatientPaged_returnsOkWithPage() throws Exception {
        Page<Encounter> page = new PageImpl<>(List.of(sampleEncounter));
        when(encounterService.findByPatientId(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/v1/encounters/patient/1/paged"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].encounterNumber").value("ENC-2024-000001"));
    }

    @Test
    void getByProvider_returnsOkWithList() throws Exception {
        when(encounterService.findByProviderId(1L))
                .thenReturn(List.of(sampleEncounter));

        mockMvc.perform(get("/v1/encounters/provider/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].encounterNumber").value("ENC-2024-000001"));
    }

    @Test
    void getProviderSchedule_returnsOk() throws Exception {
        when(encounterService.getProviderSchedule(eq(1L), eq(LocalDate.of(2024, 1, 15))))
                .thenReturn(List.of(sampleEncounter));

        mockMvc.perform(get("/v1/encounters/provider/1/schedule")
                        .param("date", "2024-01-15"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].encounterNumber").value("ENC-2024-000001"));
    }

    @Test
    void getByDateRange_returnsOk() throws Exception {
        when(encounterService.findByDateRange(
                LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31)))
                .thenReturn(List.of(sampleEncounter));

        mockMvc.perform(get("/v1/encounters/date-range")
                        .param("startDate", "2024-01-01")
                        .param("endDate", "2024-01-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].encounterNumber").value("ENC-2024-000001"));
    }

    @Test
    void getByStatus_returnsOk() throws Exception {
        when(encounterService.findByStatus(EncounterStatus.SCHEDULED))
                .thenReturn(List.of(sampleEncounter));

        mockMvc.perform(get("/v1/encounters/status/SCHEDULED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("SCHEDULED"));
    }

    @Test
    void create_returnsEncounter() throws Exception {
        when(encounterService.create(any(Encounter.class))).thenReturn(sampleEncounter);

        String body = objectMapper.writeValueAsString(sampleEncounter);

        mockMvc.perform(post("/v1/encounters")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.encounterNumber").value("ENC-2024-000001"));
    }

    @Test
    void update_returnsOk() throws Exception {
        when(encounterService.findById(1L)).thenReturn(Optional.of(sampleEncounter));
        when(encounterService.update(any(Encounter.class))).thenReturn(sampleEncounter);

        String body = objectMapper.writeValueAsString(sampleEncounter);

        mockMvc.perform(put("/v1/encounters/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.encounterNumber").value("ENC-2024-000001"));
    }

    @Test
    void update_returns404WhenNotFound() throws Exception {
        when(encounterService.findById(999L)).thenReturn(Optional.empty());

        String body = objectMapper.writeValueAsString(sampleEncounter);

        mockMvc.perform(put("/v1/encounters/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void checkIn_returnsOk() throws Exception {
        doNothing().when(encounterService).checkIn(1L);

        mockMvc.perform(post("/v1/encounters/1/check-in"))
                .andExpect(status().isOk());

        verify(encounterService).checkIn(1L);
    }

    @Test
    void start_returnsOk() throws Exception {
        doNothing().when(encounterService).startEncounter(1L);

        mockMvc.perform(post("/v1/encounters/1/start"))
                .andExpect(status().isOk());

        verify(encounterService).startEncounter(1L);
    }

    @Test
    void complete_returnsOkWithNotes() throws Exception {
        doNothing().when(encounterService).completeEncounter(1L, "Visit completed");

        mockMvc.perform(post("/v1/encounters/1/complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"Visit completed\""))
                .andExpect(status().isOk());

        verify(encounterService).completeEncounter(eq(1L), any());
    }

    @Test
    void complete_returnsOkWithoutNotes() throws Exception {
        doNothing().when(encounterService).completeEncounter(eq(1L), any());

        mockMvc.perform(post("/v1/encounters/1/complete"))
                .andExpect(status().isOk());
    }

    @Test
    void cancel_returnsOk() throws Exception {
        doNothing().when(encounterService).cancelEncounter(1L);

        mockMvc.perform(post("/v1/encounters/1/cancel"))
                .andExpect(status().isOk());

        verify(encounterService).cancelEncounter(1L);
    }

    @Test
    void noShow_returnsOk() throws Exception {
        doNothing().when(encounterService).markNoShow(1L);

        mockMvc.perform(post("/v1/encounters/1/no-show"))
                .andExpect(status().isOk());

        verify(encounterService).markNoShow(1L);
    }
}
