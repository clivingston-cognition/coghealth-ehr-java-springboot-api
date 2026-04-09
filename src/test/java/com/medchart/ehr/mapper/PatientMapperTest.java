package com.medchart.ehr.mapper;

import com.medchart.ehr.domain.patient.Address;
import com.medchart.ehr.domain.patient.EmergencyContact;
import com.medchart.ehr.domain.patient.Gender;
import com.medchart.ehr.domain.patient.IdentifierType;
import com.medchart.ehr.domain.patient.Patient;
import com.medchart.ehr.domain.patient.PatientIdentifier;
import com.medchart.ehr.dto.AddressDTO;
import com.medchart.ehr.dto.EmergencyContactDTO;
import com.medchart.ehr.dto.PatientDTO;
import com.medchart.ehr.dto.PatientIdentifierDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
@ActiveProfiles("test")
class PatientMapperTest {

    @Autowired
    private PatientMapper patientMapper;

    @Test
    void toDto_mapsAllFieldsFromEntity() {
        Address address = Address.builder()
                .street1("123 Main St")
                .city("Springfield")
                .state("IL")
                .zipCode("62701")
                .country("USA")
                .build();

        Patient patient = Patient.builder()
                .id(1L)
                .mrn("MRN001")
                .firstName("John")
                .middleName("A")
                .lastName("Doe")
                .dateOfBirth(LocalDate.of(1985, 3, 15))
                .gender(Gender.MALE)
                .email("john.doe@example.com")
                .phoneHome("555-1234")
                .address(address)
                .active(true)
                .build();

        PatientDTO dto = patientMapper.toDto(patient);

        assertEquals(1L, dto.getId());
        assertEquals("MRN001", dto.getMrn());
        assertEquals("John", dto.getFirstName());
        assertEquals("A", dto.getMiddleName());
        assertEquals("Doe", dto.getLastName());
        assertEquals(LocalDate.of(1985, 3, 15), dto.getDateOfBirth());
        assertEquals(Gender.MALE, dto.getGender());
        assertEquals("john.doe@example.com", dto.getEmail());
        assertEquals("555-1234", dto.getPhoneHome());
        assertNotNull(dto.getAddress());
        assertEquals("123 Main St", dto.getAddress().getStreet1());
        assertEquals("Springfield", dto.getAddress().getCity());
    }

    @Test
    void toEntity_mapsFromDtoAndIgnoresAuditFields() {
        PatientDTO dto = PatientDTO.builder()
                .id(99L)
                .mrn("MRN099")
                .firstName("Jane")
                .lastName("Smith")
                .dateOfBirth(LocalDate.of(1990, 6, 20))
                .gender(Gender.FEMALE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Patient entity = patientMapper.toEntity(dto);

        assertNull(entity.getId());
        assertEquals("MRN099", entity.getMrn());
        assertEquals("Jane", entity.getFirstName());
        assertEquals("Smith", entity.getLastName());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getUpdatedAt());
        assertNull(entity.getVersion());
    }

    @Test
    void updateEntityFromDto_updatesFieldsAndIgnoresNulls() {
        Patient existing = Patient.builder()
                .id(1L)
                .mrn("MRN001")
                .firstName("John")
                .lastName("Doe")
                .email("old@example.com")
                .phoneHome("555-0000")
                .active(true)
                .build();

        PatientDTO updateDto = PatientDTO.builder()
                .firstName("Johnny")
                .lastName("Updated")
                .email("new@example.com")
                .build();

        patientMapper.updateEntityFromDto(updateDto, existing);

        assertEquals(1L, existing.getId());
        assertEquals("Johnny", existing.getFirstName());
        assertEquals("Updated", existing.getLastName());
        assertEquals("new@example.com", existing.getEmail());
        assertEquals("555-0000", existing.getPhoneHome());
    }

    @Test
    void toAddressDto_mapsAddressFields() {
        Address address = Address.builder()
                .street1("456 Oak Ave")
                .street2("Suite 100")
                .city("Chicago")
                .state("IL")
                .zipCode("60601")
                .country("USA")
                .build();

        AddressDTO dto = patientMapper.toAddressDto(address);

        assertEquals("456 Oak Ave", dto.getStreet1());
        assertEquals("Suite 100", dto.getStreet2());
        assertEquals("Chicago", dto.getCity());
        assertEquals("IL", dto.getState());
        assertEquals("60601", dto.getZipCode());
        assertEquals("USA", dto.getCountry());
    }

    @Test
    void toIdentifierDto_mapsIdentifierFields() {
        PatientIdentifier identifier = PatientIdentifier.builder()
                .id(1L)
                .identifierType(IdentifierType.MRN)
                .identifierValue("MRN001")
                .issuingAuthority("MedChart")
                .active(true)
                .build();

        PatientIdentifierDTO dto = patientMapper.toIdentifierDto(identifier);

        assertEquals(1L, dto.getId());
        assertEquals(IdentifierType.MRN, dto.getIdentifierType());
        assertEquals("MRN001", dto.getIdentifierValue());
        assertEquals("MedChart", dto.getIssuingAuthority());
    }

    @Test
    void toEmergencyContactDto_mapsContactFields() {
        EmergencyContact contact = EmergencyContact.builder()
                .id(1L)
                .firstName("Jane")
                .lastName("Doe")
                .relationship("Spouse")
                .phoneHome("555-9999")
                .active(true)
                .build();

        EmergencyContactDTO dto = patientMapper.toEmergencyContactDto(contact);

        assertEquals("Jane", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
        assertEquals("Spouse", dto.getRelationship());
        assertEquals("555-9999", dto.getPhoneHome());
    }
}
