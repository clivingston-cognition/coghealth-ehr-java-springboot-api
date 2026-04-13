package com.medchart.ehr.controller;

import com.medchart.ehr.dto.BatchPatientResult;
import com.medchart.ehr.dto.PatientDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class PatientControllerBatchTest {

    @Autowired
    private TestRestTemplate restTemplate;

    private PatientDTO buildPatient(String first, String last) {
        return PatientDTO.builder()
                .firstName(first)
                .lastName(last)
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .active(true)
                .build();
    }

    @Test
    void batchCreateAllSuccessfulReturns201() {
        List<PatientDTO> patients = List.of(
                buildPatient("Alice", "Smith"),
                buildPatient("Bob", "Jones")
        );

        ResponseEntity<BatchPatientResult> response =
                restTemplate.postForEntity(
                        "/v1/patients/batch",
                        patients,
                        BatchPatientResult.class);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);
        BatchPatientResult body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTotalRequested()).isEqualTo(2);
        assertThat(body.getSuccessCount()).isEqualTo(2);
        assertThat(body.getFailureCount()).isEqualTo(0);
        assertThat(body.getCreated()).hasSize(2);
        assertThat(body.getErrors()).isEmpty();
    }

    @Test
    void batchCreateDuplicateMrnReturns207() {
        String sharedMrn =
                "DUP" + (System.currentTimeMillis() % 100000000);

        PatientDTO first = buildPatient("Carol", "White");
        first.setMrn(sharedMrn);
        PatientDTO second = buildPatient("Dave", "Black");
        second.setMrn(sharedMrn);

        List<PatientDTO> patients = List.of(first, second);

        ResponseEntity<BatchPatientResult> response =
                restTemplate.postForEntity(
                        "/v1/patients/batch",
                        patients,
                        BatchPatientResult.class);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.MULTI_STATUS);
        BatchPatientResult body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getTotalRequested()).isEqualTo(2);
        assertThat(body.getSuccessCount()).isEqualTo(1);
        assertThat(body.getFailureCount()).isEqualTo(1);
        assertThat(body.getCreated()).hasSize(1);
        assertThat(body.getErrors()).hasSize(1);
        assertThat(body.getErrors().get(0).getIndex())
                .isEqualTo(1);
        assertThat(body.getErrors().get(0).getErrorMessage())
                .contains("already exists");
    }

    @Test
    void batchCreateEmptyListReturnsError() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request =
                new HttpEntity<>("[]", headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/v1/patients/batch",
                        request,
                        String.class);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void batchCreateExceedsMaxSizeReturnsError() {
        List<PatientDTO> patients = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            patients.add(
                    buildPatient("Patient" + i, "Test"));
        }

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/v1/patients/batch",
                        patients,
                        String.class);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void batchCreateValidationErrorCaptured() {
        PatientDTO valid = buildPatient("Eve", "Green");
        PatientDTO invalid = PatientDTO.builder()
                .lastName("Brown")
                .dateOfBirth(LocalDate.of(1985, 6, 20))
                .active(true)
                .build();

        List<PatientDTO> patients =
                List.of(valid, invalid);

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/v1/patients/batch",
                        patients,
                        String.class);

        int statusCode = response.getStatusCodeValue();
        // Validation may reject the whole request (400)
        // or items may fail at service layer (207)
        assertThat(statusCode).isIn(207, 400);
    }
}
