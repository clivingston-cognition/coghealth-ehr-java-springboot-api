package com.medchart.ehr.service;

import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.dto.PatientDTO;
import com.medchart.ehr.mapper.PatientMapper;
import com.medchart.ehr.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientService patientService;

    private Patient createSamplePatient() {
        return Patient.builder()
                .id(1L)
                .mrn("MRN001")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1985, 3, 15))
                .active(true)
                .build();
    }

    private PatientDTO createSampleDto() {
        return PatientDTO.builder()
                .id(1L)
                .mrn("MRN001")
                .firstName("John")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1985, 3, 15))
                .active(true)
                .build();
    }

    @Test
    void getPatientById_returnsDtoWhenFound() {
        Patient patient = createSamplePatient();
        PatientDTO dto = createSampleDto();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientMapper.toDto(patient)).thenReturn(dto);

        PatientDTO result = patientService.getPatientById(1L);

        assertEquals("MRN001", result.getMrn());
        assertEquals("John", result.getFirstName());
        verify(patientRepository).findById(1L);
    }

    @Test
    void getPatientById_throwsWhenNotFound() {
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> patientService.getPatientById(999L));
    }

    @Test
    void getPatientByMrn_returnsDtoWhenFound() {
        Patient patient = createSamplePatient();
        PatientDTO dto = createSampleDto();

        when(patientRepository.findByMrn("MRN001")).thenReturn(Optional.of(patient));
        when(patientMapper.toDto(patient)).thenReturn(dto);

        PatientDTO result = patientService.getPatientByMrn("MRN001");

        assertEquals("MRN001", result.getMrn());
    }

    @Test
    void getPatientByMrn_throwsWhenNotFound() {
        when(patientRepository.findByMrn("INVALID")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> patientService.getPatientByMrn("INVALID"));
    }

    @Test
    void searchPatients_delegatesToRepoAndMapsResults() {
        Patient patient = createSamplePatient();
        PatientDTO dto = createSampleDto();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Patient> patientPage = new PageImpl<>(List.of(patient));

        when(patientRepository.searchPatients("smith", pageable)).thenReturn(patientPage);
        when(patientMapper.toDto(patient)).thenReturn(dto);

        Page<PatientDTO> result = patientService.searchPatients("smith", pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("John", result.getContent().get(0).getFirstName());
    }

    @Test
    void createPatient_savesAndReturnsDto() {
        PatientDTO inputDto = PatientDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1990, 6, 20))
                .build();

        Patient entity = Patient.builder()
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1990, 6, 20))
                .build();

        Patient saved = Patient.builder()
                .id(2L)
                .mrn("MRN1234")
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1990, 6, 20))
                .build();

        PatientDTO resultDto = PatientDTO.builder()
                .id(2L)
                .mrn("MRN1234")
                .firstName("Jane")
                .lastName("Smith")
                .build();

        when(patientMapper.toEntity(inputDto)).thenReturn(entity);
        when(patientRepository.save(entity)).thenReturn(saved);
        when(patientMapper.toDto(saved)).thenReturn(resultDto);

        PatientDTO result = patientService.createPatient(inputDto);

        assertNotNull(result);
        assertEquals("Jane", result.getFirstName());
        verify(patientRepository).save(entity);
    }

    @Test
    void createPatient_generatesMrnWhenNull() {
        PatientDTO inputDto = PatientDTO.builder()
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1990, 6, 20))
                .build();

        Patient entity = Patient.builder()
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1990, 6, 20))
                .build();

        Patient saved = Patient.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .build();

        PatientDTO resultDto = PatientDTO.builder()
                .id(2L)
                .firstName("Jane")
                .lastName("Smith")
                .build();

        when(patientMapper.toEntity(inputDto)).thenReturn(entity);
        when(patientRepository.save(entity)).thenReturn(saved);
        when(patientMapper.toDto(saved)).thenReturn(resultDto);

        patientService.createPatient(inputDto);

        assertNotNull(entity.getMrn());
    }

    @Test
    void createPatient_throwsOnDuplicateMrn() {
        PatientDTO inputDto = PatientDTO.builder()
                .mrn("EXISTING-MRN")
                .firstName("Jane")
                .lastName("Smith")
                .build();

        when(patientRepository.findByMrn("EXISTING-MRN"))
                .thenReturn(Optional.of(createSamplePatient()));

        assertThrows(IllegalArgumentException.class,
                () -> patientService.createPatient(inputDto));
    }

    @Test
    void updatePatient_updatesExistingAndReturnsDto() {
        Patient existing = createSamplePatient();
        PatientDTO updateDto = PatientDTO.builder()
                .firstName("John")
                .lastName("Updated")
                .build();

        Patient saved = Patient.builder()
                .id(1L)
                .mrn("MRN001")
                .firstName("John")
                .lastName("Updated")
                .build();

        PatientDTO resultDto = PatientDTO.builder()
                .id(1L)
                .mrn("MRN001")
                .firstName("John")
                .lastName("Updated")
                .build();

        when(patientRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(patientRepository.save(existing)).thenReturn(saved);
        when(patientMapper.toDto(saved)).thenReturn(resultDto);

        PatientDTO result = patientService.updatePatient(1L, updateDto);

        assertEquals("Updated", result.getLastName());
        verify(patientMapper).updateEntityFromDto(updateDto, existing);
    }

    @Test
    void updatePatient_throwsWhenNotFound() {
        when(patientRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> patientService.updatePatient(999L, createSampleDto()));
    }

    @Test
    void findBySsn_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class,
                () -> patientService.findBySsn("123-45-6789"));
    }
}
