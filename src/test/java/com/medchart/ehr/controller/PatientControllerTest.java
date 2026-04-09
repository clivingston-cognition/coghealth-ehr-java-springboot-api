package com.medchart.ehr.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medchart.ehr.config.JwtTokenProvider;
import com.medchart.ehr.dto.PatientDTO;
import com.medchart.ehr.service.PatientService;
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

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private ObjectMapper objectMapper;
    private PatientDTO samplePatient;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        samplePatient = PatientDTO.builder()
                .id(1L)
                .mrn("MRN001")
                .firstName("John")
                .middleName("A")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1985, 3, 15))
                .active(true)
                .build();
    }

    @Test
    void getPatient_returnsOkWithValidId() throws Exception {
        when(patientService.getPatientById(1L)).thenReturn(samplePatient);

        mockMvc.perform(get("/v1/patients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.mrn").value("MRN001"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    void getPatient_throwsWhenNotFound() {
        when(patientService.getPatientById(999L))
                .thenThrow(new EntityNotFoundException("Patient not found with id: 999"));

        // No @ControllerAdvice maps EntityNotFoundException to 404,
        // so the exception propagates as NestedServletException
        Exception ex = assertThrows(Exception.class,
                () -> mockMvc.perform(get("/v1/patients/999")));
        assertTrue(ex.getCause() instanceof EntityNotFoundException);
    }

    @Test
    void getPatientByMrn_returnsOkWithValidMrn() throws Exception {
        when(patientService.getPatientByMrn("MRN001")).thenReturn(samplePatient);

        mockMvc.perform(get("/v1/patients/mrn/MRN001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mrn").value("MRN001"))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    void getPatientByMrn_throwsWhenNotFound() {
        when(patientService.getPatientByMrn("INVALID"))
                .thenThrow(new EntityNotFoundException("Patient not found with MRN: INVALID"));

        Exception ex = assertThrows(Exception.class,
                () -> mockMvc.perform(get("/v1/patients/mrn/INVALID")));
        assertTrue(ex.getCause() instanceof EntityNotFoundException);
    }

    @Test
    void searchPatients_returnsOkWithPaginatedResults() throws Exception {
        Page<PatientDTO> page = new PageImpl<>(List.of(samplePatient));
        when(patientService.searchPatients(eq("smith"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/v1/patients/search").param("q", "smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("John"));
    }

    @Test
    void createPatient_returns201WithValidBody() throws Exception {
        PatientDTO input = PatientDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1990, 6, 20))
                .build();

        PatientDTO created = PatientDTO.builder()
                .id(2L)
                .mrn("MRN002")
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1990, 6, 20))
                .build();

        when(patientService.createPatient(any(PatientDTO.class))).thenReturn(created);

        mockMvc.perform(post("/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.mrn").value("MRN002"));
    }

    @Test
    void createPatient_returns400WithMissingRequiredFields() throws Exception {
        PatientDTO input = PatientDTO.builder().build();

        mockMvc.perform(post("/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatePatient_returnsOkWithValidUpdate() throws Exception {
        PatientDTO updated = PatientDTO.builder()
                .id(1L)
                .mrn("MRN001")
                .firstName("John")
                .lastName("Updated")
                .dateOfBirth(LocalDate.of(1985, 3, 15))
                .build();

        when(patientService.updatePatient(eq(1L), any(PatientDTO.class))).thenReturn(updated);

        mockMvc.perform(put("/v1/patients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Updated"));
    }

    @Test
    void updatePatient_throwsWhenNotFound() {
        when(patientService.updatePatient(eq(999L), any(PatientDTO.class)))
                .thenThrow(new EntityNotFoundException("Patient not found with id: 999"));

        PatientDTO input = PatientDTO.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

        Exception ex = assertThrows(Exception.class,
                () -> mockMvc.perform(put("/v1/patients/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input))));
        assertTrue(ex.getCause() instanceof EntityNotFoundException);
    }
}
