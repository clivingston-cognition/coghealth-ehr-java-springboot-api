package com.medchart.ehr.repository;

import com.medchart.ehr.domain.patient.Gender;
import com.medchart.ehr.domain.patient.Patient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class PatientRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PatientRepository patientRepository;

    private Patient patient1;
    private Patient patient2;

    @BeforeEach
    void setUp() {
        patient1 = Patient.builder()
                .mrn("MRN001")
                .firstName("John")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1985, 3, 15))
                .gender(Gender.MALE)
                .active(true)
                .build();
        entityManager.persistAndFlush(patient1);

        patient2 = Patient.builder()
                .mrn("MRN002")
                .firstName("Jane")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1990, 6, 20))
                .gender(Gender.FEMALE)
                .active(true)
                .build();
        entityManager.persistAndFlush(patient2);
    }

    @Test
    void searchPatients_matchesFirstName() {
        Page<Patient> results = patientRepository.searchPatients(
                "John", PageRequest.of(0, 10));

        assertEquals(1, results.getTotalElements());
        assertEquals("John", results.getContent().get(0).getFirstName());
    }

    @Test
    void searchPatients_matchesLastName() {
        Page<Patient> results = patientRepository.searchPatients(
                "Smith", PageRequest.of(0, 10));

        assertEquals(1, results.getTotalElements());
        assertEquals("Smith", results.getContent().get(0).getLastName());
    }

    @Test
    void searchPatients_matchesMrn() {
        Page<Patient> results = patientRepository.searchPatients(
                "MRN001", PageRequest.of(0, 10));

        assertEquals(1, results.getTotalElements());
        assertEquals("MRN001", results.getContent().get(0).getMrn());
    }

    @Test
    void findByMrn_returnsPatientForExactMatch() {
        Optional<Patient> result = patientRepository.findByMrn("MRN001");

        assertTrue(result.isPresent());
        assertEquals("John", result.get().getFirstName());
    }

    @Test
    void findByMrn_returnsEmptyForNoMatch() {
        Optional<Patient> result = patientRepository.findByMrn("NONEXISTENT");

        assertFalse(result.isPresent());
    }

    @Test
    void findByLastNameAndDob_returnsMatchingPatients() {
        List<Patient> results = patientRepository.findByLastNameAndDob(
                "Smith", LocalDate.of(1985, 3, 15));

        assertEquals(1, results.size());
        assertEquals("John", results.get(0).getFirstName());
    }

    @Test
    void findByLastNameAndDob_returnsEmptyForNoMatch() {
        List<Patient> results = patientRepository.findByLastNameAndDob(
                "Smith", LocalDate.of(2000, 1, 1));

        assertTrue(results.isEmpty());
    }

    @Test
    void countActivePatients_returnsCorrectCount() {
        long count = patientRepository.countActivePatients();

        assertEquals(2, count);
    }

    @Test
    void countActivePatients_excludesInactive() {
        Patient inactive = Patient.builder()
                .mrn("MRN003")
                .firstName("Bob")
                .lastName("Inactive")
                .dateOfBirth(LocalDate.of(1970, 1, 1))
                .active(false)
                .build();
        entityManager.persistAndFlush(inactive);

        long count = patientRepository.countActivePatients();

        assertEquals(2, count);
    }
}
