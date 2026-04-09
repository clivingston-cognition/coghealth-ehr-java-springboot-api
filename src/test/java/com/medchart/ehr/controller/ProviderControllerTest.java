package com.medchart.ehr.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medchart.ehr.config.JwtTokenProvider;
import com.medchart.ehr.domain.provider.Provider;
import com.medchart.ehr.domain.provider.ProviderType;
import com.medchart.ehr.service.ProviderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProviderController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ProviderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProviderService providerService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private ObjectMapper objectMapper;
    private Provider sampleProvider;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        sampleProvider = Provider.builder()
                .id(1L)
                .npi("1234567890")
                .firstName("Jane")
                .lastName("Smith")
                .providerType(ProviderType.PHYSICIAN)
                .specialty("Internal Medicine")
                .department("Medicine")
                .active(true)
                .build();
    }

    @Test
    void getAll_returnsOkWithAllProviders() throws Exception {
        when(providerService.findAll()).thenReturn(List.of(sampleProvider));

        mockMvc.perform(get("/v1/providers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].npi").value("1234567890"));
    }

    @Test
    void getAll_withActiveFilter_returnsActiveOnly() throws Exception {
        when(providerService.findActive()).thenReturn(List.of(sampleProvider));

        mockMvc.perform(get("/v1/providers").param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].active").value(true));
    }

    @Test
    void getById_returnsOk() throws Exception {
        when(providerService.findById(1L)).thenReturn(Optional.of(sampleProvider));

        mockMvc.perform(get("/v1/providers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.npi").value("1234567890"))
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    void getById_returns404WhenNotFound() throws Exception {
        when(providerService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/providers/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByNpi_returnsOk() throws Exception {
        when(providerService.findByNpi("1234567890"))
                .thenReturn(Optional.of(sampleProvider));

        mockMvc.perform(get("/v1/providers/npi/1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.npi").value("1234567890"));
    }

    @Test
    void getByNpi_returns404WhenNotFound() throws Exception {
        when(providerService.findByNpi("0000000000")).thenReturn(Optional.empty());

        mockMvc.perform(get("/v1/providers/npi/0000000000"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByDepartment_returnsOk() throws Exception {
        when(providerService.findByDepartment("Medicine"))
                .thenReturn(List.of(sampleProvider));

        mockMvc.perform(get("/v1/providers/department/Medicine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].department").value("Medicine"));
    }

    @Test
    void getBySpecialty_returnsOk() throws Exception {
        when(providerService.findBySpecialty("Internal Medicine"))
                .thenReturn(List.of(sampleProvider));

        mockMvc.perform(get("/v1/providers/specialty/Internal Medicine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].specialty").value("Internal Medicine"));
    }

    @Test
    void getDepartments_returnsListOfStrings() throws Exception {
        when(providerService.getAllDepartments())
                .thenReturn(List.of("Medicine", "Surgery", "Pediatrics"));

        mockMvc.perform(get("/v1/providers/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Medicine"))
                .andExpect(jsonPath("$[1]").value("Surgery"));
    }

    @Test
    void getSpecialties_returnsListOfStrings() throws Exception {
        when(providerService.getAllSpecialties())
                .thenReturn(List.of("Internal Medicine", "Cardiology"));

        mockMvc.perform(get("/v1/providers/specialties"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0]").value("Internal Medicine"));
    }

    @Test
    void search_returnsOk() throws Exception {
        when(providerService.search("Smith")).thenReturn(List.of(sampleProvider));

        mockMvc.perform(get("/v1/providers/search").param("lastName", "Smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].lastName").value("Smith"));
    }

    @Test
    void create_returnsProvider() throws Exception {
        when(providerService.save(any(Provider.class))).thenReturn(sampleProvider);

        mockMvc.perform(post("/v1/providers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleProvider)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.npi").value("1234567890"));
    }

    @Test
    void update_returnsOk() throws Exception {
        when(providerService.findById(1L)).thenReturn(Optional.of(sampleProvider));
        when(providerService.save(any(Provider.class))).thenReturn(sampleProvider);

        mockMvc.perform(put("/v1/providers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleProvider)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.npi").value("1234567890"));
    }

    @Test
    void update_returns404WhenNotFound() throws Exception {
        when(providerService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(put("/v1/providers/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleProvider)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deactivate_returns204() throws Exception {
        doNothing().when(providerService).deactivate(1L);

        mockMvc.perform(delete("/v1/providers/1"))
                .andExpect(status().isNoContent());

        verify(providerService).deactivate(1L);
    }
}
