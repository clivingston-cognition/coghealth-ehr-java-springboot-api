package com.medchart.ehr.repository;

import com.medchart.ehr.domain.encounter.Encounter;
import com.medchart.ehr.domain.encounter.EncounterStatus;
import com.medchart.ehr.domain.encounter.EncounterType;
import com.medchart.ehr.domain.patient.Gender;
import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.domain.provider.Provider;
import com.medchart.ehr.domain.provider.ProviderType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
class EncounterRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EncounterRepository encounterRepository;

    private Patient patient;
    private Provider provider;
    private Encounter encounter;

    @BeforeEach
    void setUp() {
        patient = Patient.builder()
                .mrn("MRN-TEST-001")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(java.time.LocalDate.of(1985, 3, 15))
                .gender(Gender.MALE)
                .active(true)
                .build();
        entityManager.persistAndFlush(patient);

        provider = Provider.builder()
                .npi("9999999999")
                .firstName("Dr")
                .lastName("Smith")
                .providerType(ProviderType.PHYSICIAN)
                .active(true)
                .acceptingPatients(true)
                .build();
        entityManager.persistAndFlush(provider);

        encounter = Encounter.builder()
                .encounterNumber("ENC-TEST-000001")
                .patient(patient)
                .attendingProvider(provider)
                .encounterType(EncounterType.OFFICE_VISIT)
                .status(EncounterStatus.SCHEDULED)
                .encounterDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .build();
        entityManager.persistAndFlush(encounter);
    }

    @Test
    void findByIdWithDetails_returnsEncounterWithPatientAndProvider() {
        Optional<Encounter> result = encounterRepository.findByIdWithDetails(encounter.getId());

        assertTrue(result.isPresent());
        Encounter found = result.get();
        assertNotNull(found.getPatient());
        assertEquals("John", found.getPatient().getFirstName());
        assertNotNull(found.getAttendingProvider());
        assertEquals("Smith", found.getAttendingProvider().getLastName());
    }

    @Test
    void findTodaysSchedule_filtersCorrectly() {
        LocalDateTime startOfDay = LocalDateTime.of(2024, 1, 15, 0, 0);
        LocalDateTime endOfDay = LocalDateTime.of(2024, 1, 16, 0, 0);

        List<Encounter> result = encounterRepository.findTodaysSchedule(
                provider.getId(), startOfDay, endOfDay);

        assertEquals(1, result.size());
        assertEquals("ENC-TEST-000001", result.get(0).getEncounterNumber());
    }

    @Test
    void findTodaysSchedule_excludesCompletedEncounters() {
        Encounter completed = Encounter.builder()
                .encounterNumber("ENC-TEST-000002")
                .patient(patient)
                .attendingProvider(provider)
                .encounterType(EncounterType.OFFICE_VISIT)
                .status(EncounterStatus.COMPLETED)
                .encounterDateTime(LocalDateTime.of(2024, 1, 15, 14, 0))
                .build();
        entityManager.persistAndFlush(completed);

        LocalDateTime startOfDay = LocalDateTime.of(2024, 1, 15, 0, 0);
        LocalDateTime endOfDay = LocalDateTime.of(2024, 1, 16, 0, 0);

        List<Encounter> result = encounterRepository.findTodaysSchedule(
                provider.getId(), startOfDay, endOfDay);

        assertEquals(1, result.size());
    }

    @Test
    void findByDateRange_returnsEncountersInRange() {
        LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 2, 1, 0, 0);

        List<Encounter> result = encounterRepository.findByDateRange(start, end);

        assertEquals(1, result.size());
    }

    @Test
    void findByDateRange_excludesOutOfRange() {
        LocalDateTime start = LocalDateTime.of(2024, 2, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2024, 3, 1, 0, 0);

        List<Encounter> result = encounterRepository.findByDateRange(start, end);

        assertTrue(result.isEmpty());
    }

    @Test
    void countByPatientId_returnsCorrectCount() {
        long count = encounterRepository.countByPatientId(patient.getId());

        assertEquals(1, count);
    }

    @Test
    void countByPatientId_returnsZeroForNoEncounters() {
        Patient otherPatient = Patient.builder()
                .mrn("MRN-TEST-002")
                .firstName("Other")
                .lastName("Patient")
                .dateOfBirth(java.time.LocalDate.of(1990, 1, 1))
                .active(true)
                .build();
        entityManager.persistAndFlush(otherPatient);

        long count = encounterRepository.countByPatientId(otherPatient.getId());

        assertEquals(0, count);
    }
}
