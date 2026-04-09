package com.medchart.ehr.service;

import com.medchart.ehr.domain.encounter.Encounter;
import com.medchart.ehr.domain.encounter.EncounterStatus;
import com.medchart.ehr.domain.encounter.EncounterType;
import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.domain.provider.Provider;
import com.medchart.ehr.domain.provider.ProviderType;
import com.medchart.ehr.repository.EncounterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EncounterServiceTest {

    @Mock
    private EncounterRepository encounterRepository;

    @InjectMocks
    private EncounterService encounterService;

    private Encounter sampleEncounter;
    private Patient samplePatient;

    @BeforeEach
    void setUp() {
        samplePatient = Patient.builder()
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
                .patient(samplePatient)
                .attendingProvider(provider)
                .encounterType(EncounterType.OFFICE_VISIT)
                .status(EncounterStatus.SCHEDULED)
                .encounterDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .build();
    }

    @Test
    void findById_delegatesToRepository() {
        when(encounterRepository.findById(1L)).thenReturn(Optional.of(sampleEncounter));

        Optional<Encounter> result = encounterService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("ENC-2024-000001", result.get().getEncounterNumber());
    }

    @Test
    void findByIdWithDetails_delegatesToRepository() {
        when(encounterRepository.findByIdWithDetails(1L))
                .thenReturn(Optional.of(sampleEncounter));

        Optional<Encounter> result = encounterService.findByIdWithDetails(1L);

        assertTrue(result.isPresent());
        verify(encounterRepository).findByIdWithDetails(1L);
    }

    @Test
    void findByEncounterNumber_delegatesToRepository() {
        when(encounterRepository.findByEncounterNumber("ENC-2024-000001"))
                .thenReturn(Optional.of(sampleEncounter));

        Optional<Encounter> result = encounterService.findByEncounterNumber("ENC-2024-000001");

        assertTrue(result.isPresent());
    }

    @Test
    void findByPatientId_returnsList() {
        when(encounterRepository.findByPatientId(1L))
                .thenReturn(List.of(sampleEncounter));

        List<Encounter> result = encounterService.findByPatientId(1L);

        assertEquals(1, result.size());
    }

    @Test
    void findByPatientIdPaged_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Encounter> page = new PageImpl<>(List.of(sampleEncounter));
        when(encounterRepository.findByPatientId(1L, pageable)).thenReturn(page);

        Page<Encounter> result = encounterService.findByPatientId(1L, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findByProviderId_delegatesToRepository() {
        when(encounterRepository.findByAttendingProviderId(1L))
                .thenReturn(List.of(sampleEncounter));

        List<Encounter> result = encounterService.findByProviderId(1L);

        assertEquals(1, result.size());
        verify(encounterRepository).findByAttendingProviderId(1L);
    }

    @Test
    void getProviderSchedule_convertsDateToRange() {
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay();

        when(encounterRepository.findTodaysSchedule(1L, startOfDay, endOfDay))
                .thenReturn(List.of(sampleEncounter));

        List<Encounter> result = encounterService.getProviderSchedule(1L, date);

        assertEquals(1, result.size());
        verify(encounterRepository).findTodaysSchedule(1L, startOfDay, endOfDay);
    }

    @Test
    void findByDateRange_delegatesToRepository() {
        LocalDate startDate = LocalDate.of(2024, 1, 1);
        LocalDate endDate = LocalDate.of(2024, 1, 31);

        when(encounterRepository.findByDateRange(
                startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay()))
                .thenReturn(List.of(sampleEncounter));

        List<Encounter> result = encounterService.findByDateRange(startDate, endDate);

        assertEquals(1, result.size());
    }

    @Test
    void findByStatus_delegatesToRepository() {
        when(encounterRepository.findByStatus(EncounterStatus.SCHEDULED))
                .thenReturn(List.of(sampleEncounter));

        List<Encounter> result = encounterService.findByStatus(EncounterStatus.SCHEDULED);

        assertEquals(1, result.size());
    }

    @Test
    void create_setsEncounterNumberAndScheduledStatus() {
        Encounter newEncounter = Encounter.builder()
                .patient(samplePatient)
                .encounterType(EncounterType.OFFICE_VISIT)
                .encounterDateTime(LocalDateTime.of(2024, 1, 15, 10, 0))
                .build();

        when(encounterRepository.save(any(Encounter.class))).thenReturn(sampleEncounter);

        Encounter result = encounterService.create(newEncounter);

        assertNotNull(newEncounter.getEncounterNumber());
        assertEquals(EncounterStatus.SCHEDULED, newEncounter.getStatus());
        assertNotNull(result);
    }

    @Test
    void update_savesAndReturns() {
        when(encounterRepository.save(sampleEncounter)).thenReturn(sampleEncounter);

        Encounter result = encounterService.update(sampleEncounter);

        assertEquals("ENC-2024-000001", result.getEncounterNumber());
        verify(encounterRepository).save(sampleEncounter);
    }

    @Test
    void checkIn_setsStatusToCheckedIn() {
        when(encounterRepository.findById(1L)).thenReturn(Optional.of(sampleEncounter));

        encounterService.checkIn(1L);

        assertEquals(EncounterStatus.CHECKED_IN, sampleEncounter.getStatus());
        verify(encounterRepository).save(sampleEncounter);
    }

    @Test
    void checkIn_doesNothingWhenNotFound() {
        when(encounterRepository.findById(999L)).thenReturn(Optional.empty());

        encounterService.checkIn(999L);

        verify(encounterRepository, never()).save(any());
    }

    @Test
    void startEncounter_setsStatusToInProgress() {
        when(encounterRepository.findById(1L)).thenReturn(Optional.of(sampleEncounter));

        encounterService.startEncounter(1L);

        assertEquals(EncounterStatus.IN_PROGRESS, sampleEncounter.getStatus());
        verify(encounterRepository).save(sampleEncounter);
    }

    @Test
    void startEncounter_doesNothingWhenNotFound() {
        when(encounterRepository.findById(999L)).thenReturn(Optional.empty());

        encounterService.startEncounter(999L);

        verify(encounterRepository, never()).save(any());
    }

    @Test
    void completeEncounter_setsStatusToCompletedWithNotes() {
        when(encounterRepository.findById(1L)).thenReturn(Optional.of(sampleEncounter));

        encounterService.completeEncounter(1L, "Patient discharged");

        assertEquals(EncounterStatus.COMPLETED, sampleEncounter.getStatus());
        assertEquals("Patient discharged", sampleEncounter.getNotes());
        verify(encounterRepository).save(sampleEncounter);
    }

    @Test
    void completeEncounter_setsStatusToCompletedWithoutNotes() {
        when(encounterRepository.findById(1L)).thenReturn(Optional.of(sampleEncounter));

        encounterService.completeEncounter(1L, null);

        assertEquals(EncounterStatus.COMPLETED, sampleEncounter.getStatus());
        verify(encounterRepository).save(sampleEncounter);
    }

    @Test
    void completeEncounter_doesNothingWhenNotFound() {
        when(encounterRepository.findById(999L)).thenReturn(Optional.empty());

        encounterService.completeEncounter(999L, "notes");

        verify(encounterRepository, never()).save(any());
    }

    @Test
    void cancelEncounter_setsStatusToCancelled() {
        when(encounterRepository.findById(1L)).thenReturn(Optional.of(sampleEncounter));

        encounterService.cancelEncounter(1L);

        assertEquals(EncounterStatus.CANCELLED, sampleEncounter.getStatus());
        verify(encounterRepository).save(sampleEncounter);
    }

    @Test
    void cancelEncounter_doesNothingWhenNotFound() {
        when(encounterRepository.findById(999L)).thenReturn(Optional.empty());

        encounterService.cancelEncounter(999L);

        verify(encounterRepository, never()).save(any());
    }

    @Test
    void markNoShow_setsStatusToNoShow() {
        when(encounterRepository.findById(1L)).thenReturn(Optional.of(sampleEncounter));

        encounterService.markNoShow(1L);

        assertEquals(EncounterStatus.NO_SHOW, sampleEncounter.getStatus());
        verify(encounterRepository).save(sampleEncounter);
    }

    @Test
    void markNoShow_doesNothingWhenNotFound() {
        when(encounterRepository.findById(999L)).thenReturn(Optional.empty());

        encounterService.markNoShow(999L);

        verify(encounterRepository, never()).save(any());
    }
}
